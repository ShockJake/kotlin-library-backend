package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import university.com.api.ApiTestCommons.authenticateUser
import university.com.api.ApiTestCommons.setMockEngine
import university.com.data.model.Book
import university.com.data.model.Purchase
import university.com.plugins.configureRouting
import university.com.plugins.configureSecurity
import university.com.plugins.configureSerialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PurchaseApiTest {
    private val objectMapper = jacksonObjectMapper()
    private var token = ""

    @BeforeAll
    fun beforeAll() = testApplication {
        setMockEngine()
        application { configureSecurity(); configureRouting(); configureSerialization() }
        token = authenticateUser(client)
    }

    @AfterAll
    fun afterAll() {
        ApiTestCommons.cleanup()
    }

    @Test
    fun shouldGetEmptyPurchases() = testApplication {
        // given
        application { configureSecurity(); configureRouting(); configureSerialization() }

        val expectedResult = listOf<Purchase>()

        // when
        val response = client.get("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        val actualResult = objectMapper.readValue<List<Purchase>>(responseBody)
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun shouldAddPurchase() = testApplication {
        // given
        application { configureSecurity(); configureRouting(); configureSerialization() }
        val books = listOf(Book("TITLE", "AUTHOR", "DATE", "ID"))

        // when
        val addResponse = client.post("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(objectMapper.writeValueAsString(books))
        }
        val getResponse = client.get("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // then
        assertEquals(HttpStatusCode.OK, addResponse.status)
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        assertNotNull(responseBody)
        val actualResult = objectMapper.readValue<List<Purchase>>(responseBody)
        assertEquals(books, actualResult.first().books)
    }

    @Test
    fun shouldRespondWithBadRequestIfEmptyDataProvided() = testApplication {
        // given
        application { configureSecurity(); configureRouting(); configureSerialization() }

        // when
        val response = client.post("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun shouldRespondWithBadRequestIfNotJsonProvided() = testApplication {
        // given
        application { configureSecurity(); configureRouting(); configureSerialization() }

        // when
        val response = client.post("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("<tag>SomeXML<tag>")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
