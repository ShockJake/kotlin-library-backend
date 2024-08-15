package university.com.data.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlinx.coroutines.withContext
import university.com.common.DispatcherProvider
import university.com.data.model.Book
import java.net.URLEncoder

class BookService(private val client: HttpClient, private val objectMapper: ObjectMapper) {
    private val logger: Logger = KtorSimpleLogger(BookService::class.java.simpleName)
    private val categories =
        listOf(
            "Fiction",
            "Non-fiction",
            "Mystery",
            "Thriller",
            "Science Fiction",
            "Romance",
            "Historical Fiction",
            "Biography",
            "Personal Development",
            "Philosophy",
            "Travel",
            "Science",
            "Poetry",
            "Art",
            "Photography",
            "Economics",
            "Cooking",
            "History"
        )

    fun getCategories(): List<String> {
        return categories
    }

    suspend fun fetchBooksDataAsObjects(category: String): List<Book> {
        logger.info("Fetching books data for '$category'")
        val response = doGetRequest(category)
        if (response.status.isSuccess()) {
            val searchResult: JsonNode = getSearchResult(response.body<String>())
            if (searchResult.isArray) {
                return (searchResult as ArrayNode)
                    .map { subNode -> parseBookDataAsObject(subNode) }
                    .toList()
            }
            logger.error("Search result for category - '$category' is not array")
            throw Exception("Bad data provided")
        } else {
            logger.error("Got response with bad status ${response.status.value}")
            throw Exception("Cannot fetch the data, response code - ${response.status.value}")
        }
    }

    suspend fun fetchBooksDataAsText(category: String): List<String> {
        val books = fetchBooksDataAsObjects(category)
        return books.map { book ->
            formatBookDataAsString(book)
        }.toList()
    }

    private suspend fun doGetRequest(category: String): HttpResponse {
        val encodedCategory = withContext(DispatcherProvider.getIO()) {
            URLEncoder.encode(category, "UTF-8")
        }
        val url = "https://openlibrary.org/search.json?subject=$encodedCategory&limit=10"
        logger.info("Fetching data from: $url")
        return client.get(url)
    }

    private fun getSearchResult(responseBody: String): JsonNode {
        try {
            return objectMapper.readTree(responseBody)["docs"]
        } catch (e: Exception) {
            logger.error("Cannot get search result from response: ${e.message}")
            throw Exception("Cannot get search result from response")
        }
    }

    private fun parseBookDataAsObject(input: JsonNode): Book {
        val title = parseJsonElement(input, "title")
        val author = parseJsonElement(input, "author_name", 0)
        val firstPublishedDate = parseJsonElement(input, "publish_date", 0)
        var amazonBookId = ""
        try {
            amazonBookId = parseJsonElement(input, "id_amazon", 0)
        } catch (e: Exception) {
            logger.warn("No amazon link for book: $title")
        }
        return Book(title, author, firstPublishedDate, amazonBookId)
    }

    private fun formatBookDataAsString(book: Book): String {
        return "- ${book.title} | ${book.author} | ${book.firstPublishedDate} | [Amazon Link](https://www.amazon.com/dp/${book.amazonBookId})"
    }

    private fun parseJsonElement(input: JsonNode, target: String): String {
        try {
            return input[target].asText()
        } catch (e: Exception) {
            throw Exception("Cannot parse $target: ${e.message}")
        }
    }

    private fun parseJsonElement(input: JsonNode, target: String, index: Int): String {
        try {
            return input[target][0].asText()
        } catch (e: Exception) {
            logger.error("Cannot parse $target: ${e.message}")
            throw Exception("Cannot parse $target with index '$index': ${e.message}")
        }
    }
}
