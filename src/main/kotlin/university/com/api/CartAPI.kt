package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
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
