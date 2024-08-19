package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import university.com.common.HttpClientProvider
import university.com.common.ServiceProvider.getUserService
import kotlin.test.assertEquals

object ApiTestCommons {
    const val TEST_CATEGORY = "TEST_CATEGORY"
    private const val TEST_ENDPOINT = "/search.json?subject=$TEST_CATEGORY&limit=10"
    private const val EMPTY_RESPONSE_ENDPOINT = "/search.json?subject=EMPTY&limit=10"
    private const val BAD_RESPONSE_ENDPOINT = "/search.json?subject=BAD&limit=10"
    private val objectMapper = jacksonObjectMapper()
    private val userService = getUserService()
    private var userId = ""

    suspend fun authenticateUser(client: HttpClient): String {
        val username = "USER"
        val password = "PASSWORD"

        val registerResponse = client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{ "name": "$username", "password": "$password" }""")
        }
        assertEquals(HttpStatusCode.OK, registerResponse.status)

        val loginResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{ "name": "$username", "password": "$password" }""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        userId = userService.findUserByUsername(username).id

        return objectMapper.readTree(loginResponse.bodyAsText()).get("token").asText()
    }

    fun cleanup() {
        userService.deleteUser(userId)
    }

    fun setMockEngine() {
        HttpClientProvider.setUseMock(true)
        HttpClientProvider.setMockEngine(getMockEngine())
    }

    private fun getMockEngine(): MockEngine {
        val responseBody = this.javaClass.classLoader.getResource("books.json")?.readText()
        return MockEngine { request ->
            when (request.url.fullPath) {
                TEST_ENDPOINT -> respond(
                    content = responseBody!!,
                    status = HttpStatusCode.OK
                )

                EMPTY_RESPONSE_ENDPOINT -> respond(
                    content = "",
                    status = HttpStatusCode.OK
                )

                BAD_RESPONSE_ENDPOINT -> respond(
                    content = "",
                    status = HttpStatusCode.NotFound
                )

                else -> respond(
                    content = """{"error": "Not Found"} """,
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}
