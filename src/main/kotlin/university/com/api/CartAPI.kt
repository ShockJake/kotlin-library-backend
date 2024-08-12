package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.util.logging.KtorSimpleLogger
import university.com.data.model.Book
import university.com.data.service.CartService
import university.com.data.service.PurchaseService

private val cartService = CartService(PurchaseService())
private val mapper = jacksonObjectMapper()
private val logger = KtorSimpleLogger("CartAPI")

fun Route.cartApi() {
    route("/cart") {
        get {
            call.respond(mapper.writeValueAsString(cartService.getContents()))
        }
        put {
            try {
                val requestData = call.receive<String>()
                val parsedData = mapper.readTree(requestData)
                val action = parsedData.get("action").asText()
                val book = mapper.readValue(parsedData.get("book").toString(), Book::class.java)
                handleBookAction(action, book)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        delete {
            cartService.cleanCart()
            call.respond(HttpStatusCode.OK)
        }
        get("/checkout") {
            try {
                cartService.checkout()
            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(HttpStatusCode.BadRequest)
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun handleBookAction(action: String, book: Book) {
    if ("add".equals(action, ignoreCase = true)) {
        cartService.addBook(book)
    } else if ("remove".equals(action, ignoreCase = true)) {
        cartService.removeBook(book)
    } else {
        throw Exception("Invalid action")
    }
}
