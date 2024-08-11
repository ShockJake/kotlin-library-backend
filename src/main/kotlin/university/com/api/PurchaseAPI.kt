package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import university.com.data.model.Book
import university.com.data.service.PurchaseService

private val purchaseService = PurchaseService()
private val mapper = jacksonObjectMapper()
private val logger = KtorSimpleLogger("PurchaseAPI")


fun Route.purchaseApi() {

    route("/purchase") {
        get {
            call.respond(HttpStatusCode.OK, mapper.writeValueAsString(purchaseService.getPurchases()))
        }

        post {
            try {
                val data = call.receive<String>()
                val books = mapper.readValue<List<Book>>(data)
                purchaseService.addPurchase(books)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                logger.error("Cannot register purchase: ${e.message}")
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
