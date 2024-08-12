package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.util.logging.KtorSimpleLogger
import university.com.common.HttpClientProvider
import university.com.data.service.BookService

private val objectMapper = jacksonObjectMapper()
private val bookService = BookService(HttpClientProvider.getClient(), objectMapper)
private val logger = KtorSimpleLogger("BookAPI")

fun Route.bookApi() {
    route("/books") {
        get("{category}") {
            try {
                val books = bookService.fetchBooksDataAsObjects(call.parameters["category"]!!)
                call.respond(objectMapper.writeValueAsString(books))
            } catch (e: Exception) {
                logger.error("Error: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
    }
    route("/categories") { get { call.respond(objectMapper.writeValueAsString(bookService.getCategories())) } }
}
