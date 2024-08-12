package university.com.apiTests

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
import university.com.data.model.Book
import university.com.data.model.Purchase
import university.com.plugins.configureRouting
import university.com.plugins.jsonModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PurchaseApiTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun shouldGetEmptyPurchases() = testApplication {
        // given
        application { configureRouting(); jsonModule() }
        val expectedResult = listOf<Purchase>()

        // when
        val response = client.get("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
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
        application { configureRouting(); jsonModule() }
        val books = listOf(Book("TITLE", "AUTHOR", "DATE", "ID"))

        // when
        val addResponse = client.post("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(books))
        }
        val getResponse = client.get("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
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
        application { configureRouting(); jsonModule() }

        // when
        val response = client.post("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody("")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun shouldRespondWithBadRequestIfNotJsonProvided() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val response = client.post("/purchase") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody("<tag>SomeXML<tag>")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
