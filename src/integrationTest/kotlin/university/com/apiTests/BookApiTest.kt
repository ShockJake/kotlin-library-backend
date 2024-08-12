package university.com.apiTests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.server.testing.testApplication
import university.com.common.HttpClientProvider
import university.com.data.service.DataSupplier.getBooksAsObjects
import university.com.data.service.DataSupplier.getCategories
import university.com.plugins.configureRouting
import university.com.plugins.jsonModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BookApiTest {
    private val testCategory = "TEST_CATEGORY"
    private val testEndpoint = "/search.json?subject=$testCategory&limit=10"
    private val emptyResponseEndpoint = "/search.json?subject=EMPTY&limit=10"
    private val badResponseEndpoint = "/search.json?subject=BAD&limit=10"
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun shouldGetCategories() = testApplication {
        // given
        application {
            HttpClientProvider.setUseMock(true)
            HttpClientProvider.setMockEngine(getMockEngine())
            configureRouting()
            jsonModule()
        }

        // when
        val response = client.get("/categories") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        val result = objectMapper.readValue<List<String>>(responseBody)
        assertEquals(result, getCategories())
    }

    @Test
    fun shouldGetBooksDataByCategory() = testApplication {
        // given
        val expectedResponse = objectMapper.writeValueAsString(getBooksAsObjects())
        application {
            HttpClientProvider.setUseMock(true)
            HttpClientProvider.setMockEngine(getMockEngine())
            configureRouting()
            jsonModule()
        }

        // when
        val response = client.get("/books/$testCategory") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expectedResponse, response.bodyAsText())
    }

    @Test
    fun shouldRespondWithBadResponseIfNoFetchResponseGot() = testApplication {
        // given
        application {
            HttpClientProvider.setUseMock(true)
            HttpClientProvider.setMockEngine(getMockEngine())
            configureRouting()
            jsonModule()
        }

        // when
        val response = client.get("/books/EMPTY") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Cannot get search result from response", response.bodyAsText())
    }

    @Test
    fun shouldRespondWithBadResponseIfBadFetchResponseGot() = testApplication {
        // given
        application {
            HttpClientProvider.setUseMock(true)
            HttpClientProvider.setMockEngine(getMockEngine())
            configureRouting()
            jsonModule()
        }

        // when
        val response = client.get("/books/BAD") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Cannot fetch the data, response code - 404", response.bodyAsText())
    }

    private fun getMockEngine(): MockEngine {
        val responseBody = this.javaClass.classLoader.getResource("books.json")?.readText()
        return MockEngine { request ->
            when (request.url.fullPath) {
                testEndpoint -> respond(
                    content = responseBody!!,
                    status = HttpStatusCode.OK
                )

                emptyResponseEndpoint -> respond(
                    content = "",
                    status = HttpStatusCode.OK
                )

                badResponseEndpoint -> respond(
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
