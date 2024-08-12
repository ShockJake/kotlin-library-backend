package university.com.data.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.mock
import university.com.data.model.Book
import university.com.data.service.DataSupplier.getBooksAsObjects
import university.com.data.service.DataSupplier.getBooksAsStrings
import university.com.data.service.DataSupplier.getCategories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookServiceTest {
    private val testCategory = "TEST_CATEGORY"
    private val testEndpoint = "/search.json?subject=$testCategory&limit=10"
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun shouldGetCategories() {
        // given
        val bookService = BookService(mock(), objectMapper)
        val expectedCategories = getCategories()

        // when
        val actualCategories = bookService.getCategories()

        // then
        assertEquals(expectedCategories, actualCategories)
    }

    @Test
    fun shouldFetchBooksDataAsStrings(): Unit = runBlocking {
        // given
        val expectedBooks = getBooksAsStrings()
        val responseBody = this.javaClass.classLoader.getResource("books.json")?.readText()
        val bookService = BookService(getHttpClientMock(responseBody!!, HttpStatusCode.OK), objectMapper)

        // when
        val actualBooks = bookService.fetchBooksDataAsText(testCategory)

        // then
        assertEquals(expectedBooks.size, actualBooks.size)
        assertEquals(expectedBooks[0], actualBooks[0])
        assertEquals(expectedBooks[1], actualBooks[1])
        assertEquals(expectedBooks[2], actualBooks[2])
        assertEquals(expectedBooks[3], actualBooks[3])
        assertEquals(expectedBooks[4], actualBooks[4])
        assertEquals(expectedBooks[5], actualBooks[5])
        assertEquals(expectedBooks[6], actualBooks[6])
        assertEquals(expectedBooks[7], actualBooks[7])
        assertEquals(expectedBooks[8], actualBooks[8])
        assertEquals(expectedBooks[9], actualBooks[9])
    }

    @Test
    fun shouldFetchBooksDataAsObjects(): Unit = runBlocking {
        // given
        val expectedBooks = getBooksAsObjects()
        val responseBody = this.javaClass.classLoader.getResource("books.json")?.readText()
        val bookService = BookService(getHttpClientMock(responseBody!!, HttpStatusCode.OK), objectMapper)

        // when
        val actualBooks = bookService.fetchBooksDataAsObjects(testCategory)

        // then
        assertEquals(expectedBooks.size, actualBooks.size)
        assertEquals(expectedBooks[0], actualBooks[0])
        assertEquals(expectedBooks[1], actualBooks[1])
        assertEquals(expectedBooks[2], actualBooks[2])
        assertEquals(expectedBooks[3], actualBooks[3])
        assertEquals(expectedBooks[4], actualBooks[4])
        assertEquals(expectedBooks[5], actualBooks[5])
        assertEquals(expectedBooks[6], actualBooks[6])
        assertEquals(expectedBooks[7], actualBooks[7])
        assertEquals(expectedBooks[8], actualBooks[8])
        assertEquals(expectedBooks[9], actualBooks[9])
    }

    @Test
    fun shouldThrowExceptionIfResponseIsNotSuccessful(): Unit = runBlocking {
        // given
        val responseBody = """{"error": "Not successful"}"""
        val bookService = BookService(getHttpClientMock(responseBody, HttpStatusCode.NotFound), objectMapper)

        // when & then
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsObjects(testCategory)
        }
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsText(testCategory)
        }
    }

    @Test
    fun shouldThrowExceptionIfEmptyResponseBodyProvided(): Unit = runBlocking {
        // given
        val responseBody = ""
        val bookService = BookService(getHttpClientMock(responseBody, HttpStatusCode.OK), objectMapper)

        // when & then
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsObjects(testCategory)
        }
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsText(testCategory)
        }
    }

    @Test
    fun shouldThrowExceptionIfBadResponseBodyProvided(): Unit = runBlocking {
        // given
        val responseBody = "{.,.amnmw.d}"
        val bookService = BookService(getHttpClientMock(responseBody, HttpStatusCode.OK), objectMapper)

        // when & then
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsObjects(testCategory)
        }
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsText(testCategory)
        }
    }

    @Test
    fun shouldThrowExceptionIfSearchResultIsNotJsonArray(): Unit = runBlocking {
        // given
        val responseBody = """{ "docs": "TEST" }"""
        val bookService = BookService(getHttpClientMock(responseBody, HttpStatusCode.OK), objectMapper)

        // when & then
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsObjects(testCategory)
        }
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsText(testCategory)
        }
    }

    @Test
    fun shouldThrowExceptionIfSearchResultIfIncomplete(): Unit = runBlocking {
        // given
        val responseBody = """{ "docs": [ {"title": "test_title"} ] }"""
        val bookService = BookService(getHttpClientMock(responseBody, HttpStatusCode.OK), objectMapper)

        // when & then
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsObjects(testCategory)
        }
        assertFailsWith<Exception> {
            bookService.fetchBooksDataAsText(testCategory)
        }
    }

    @Test
    fun shouldFetchBooksDataIfNoAmazonIdSupplied(): Unit = runBlocking {
        // given
        val responseBody = this.javaClass.classLoader.getResource("book.json")?.readText()!!.replace("id#1", "")
        val bookService = BookService(getHttpClientMock(responseBody, HttpStatusCode.OK), objectMapper)
        val expectedBook = Book("Title 1", "Author 1", "2024", "")

        // when
        val actualBooks = bookService.fetchBooksDataAsObjects(testCategory)

        // then
        assertEquals(1, actualBooks.size)
        assertEquals(expectedBook, actualBooks.first())
    }

    private fun getHttpClientMock(responseBody: String, httpStatus: HttpStatusCode): HttpClient {
        return HttpClient(getMockEngine(responseBody, httpStatus)) {
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
            }
        }
    }

    private fun getMockEngine(responseBody: String, httpStatus: HttpStatusCode): MockEngine {
        return MockEngine { request ->
            when (request.url.fullPath) {
                testEndpoint -> respond(
                    content = responseBody,
                    status = httpStatus
                )

                else -> respond(
                    content = """{"error": "Not Found"} """,
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}
