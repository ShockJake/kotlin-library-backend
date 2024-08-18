package university.com.apiTests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import university.com.apiTests.ApiTestCommons.TEST_CATEGORY
import university.com.apiTests.ApiTestCommons.setMockEngine
import university.com.data.service.DataSupplier.getBooksAsObjects
import university.com.data.service.DataSupplier.getCategories
import university.com.plugins.configureRouting
import university.com.plugins.configureSecurity
import university.com.plugins.configureSerialization
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookApiTest {

    private val objectMapper = jacksonObjectMapper()

    @BeforeAll
    fun beforeAll() {
        setMockEngine()
    }

    @Test
    fun shouldGetCategories() = testApplication {
        // given
        application { configureSecurity(); configureRouting(); configureSerialization() }

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
        application { configureSecurity(); configureRouting(); configureSerialization() }

        // when
        val response = client.get("/books/$TEST_CATEGORY") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(expectedResponse, response.bodyAsText())
    }

    @Test
    fun shouldRespondWithBadResponseIfNoFetchResponseGot() = testApplication {
        // given
        application { configureSecurity(); configureRouting(); configureSerialization() }

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
        application { configureSecurity(); configureRouting(); configureSerialization() }

        // when
        val response = client.get("/books/BAD") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Cannot fetch the data, response code - 404", response.bodyAsText())
    }
}
