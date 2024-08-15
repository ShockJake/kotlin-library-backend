package university.com.apiTests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import university.com.data.model.Book
import university.com.plugins.configureRouting
import university.com.plugins.jsonModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CartApiTest {
    private val objectMapper = jacksonObjectMapper()
    private val book = Book("TITLE", "AUTHOR", "DATE", "ID")

    @BeforeTest
    fun setUp() {
        cleanCart()
    }

    @Test
    fun shouldUpdateCart() = testApplication {
        // given
        application { configureRouting(); jsonModule() }
        val expectedResult = listOf(book)

        // when & then
        addBookToCart(book)
        getCartContents(expectedResult)
    }

    @Test
    fun shouldRespondWithBadRequestIfEmptyDataProvided() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val response = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody("")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun shouldRespondWithBadRequestIfInvalidActionProvided() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val response = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody("""{"action": "TEST_ACTION"}""")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun shouldRespondWithBadRequestIfIncompleteDataProvided() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val response = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody("""{"action": "add"}""")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun shouldRespondWithBadRequestIfProvidedBookDataIsIncomplete() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val response = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody("""{"action": "add", "book": {}}""")
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun shouldAddAndRemoveTheBookFromCart() = testApplication {
        // given
        application { configureRouting(); jsonModule() }
        val removeRequestBody = """{ "action": "remove", "book": ${objectMapper.writeValueAsString(book)} }"""
        val expectedResult = listOf<Book>()

        // when
        addBookToCart(book)
        val removeResponse = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(removeRequestBody)
        }
        val getResponse = client.get("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, getResponse.status)
        assertEquals(HttpStatusCode.OK, removeResponse.status)

        val responseBody = getResponse.bodyAsText()
        assertNotNull(responseBody)
        val actualResult = objectMapper.readValue<List<Book>>(responseBody)
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun shouldRespondWithBadRequestIfThereAreNoBooksToRemove() = testApplication {
        // given
        application { configureRouting(); jsonModule() }
        val removeRequestBody = """{ "action": "remove", "book": ${objectMapper.writeValueAsString(book)} }"""

        // when
        val removeResponse = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(removeRequestBody)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, removeResponse.status)
    }

    @Test
    fun shouldCheckoutCart() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        addBookToCart(book)
        val checkoutResponse = client.get("/cart/checkout") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, checkoutResponse.status)
    }

    @Test
    fun shouldRespondWithBadRequestIfNoBooksAreInCart() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val checkoutResponse = client.get("/cart/checkout") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, checkoutResponse.status)
    }

    private fun cleanCart() = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val cleanResponse = client.delete("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, cleanResponse.status)
    }

    private fun addBookToCart(book: Book) = testApplication {
        // given
        application { configureRouting(); jsonModule() }
        val addRequestBody = """{ "action": "add", "book": ${objectMapper.writeValueAsString(book)} }"""

        // when
        val addResponse = client.put("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            setBody(addRequestBody)
        }

        // then
        assertEquals(HttpStatusCode.OK, addResponse.status)
    }

    private fun getCartContents(expectedResult: List<Book>) = testApplication {
        // given
        application { configureRouting(); jsonModule() }

        // when
        val getResponse = client.get("/cart") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val responseBody = getResponse.bodyAsText()
        assertNotNull(responseBody)
        val actualResult = objectMapper.readValue<List<Book>>(responseBody)
        assertEquals(expectedResult, actualResult)
    }
}
