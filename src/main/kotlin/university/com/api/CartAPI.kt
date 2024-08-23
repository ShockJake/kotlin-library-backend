package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.util.logging.KtorSimpleLogger
import university.com.common.ServiceProvider.getCartService
import university.com.common.ServiceProvider.getUserService
import university.com.data.model.Book
import university.com.security.JwtConfig

private val cartService = getCartService()
private val userService = getUserService()
private val mapper = jacksonObjectMapper()
private val logger = KtorSimpleLogger("CartAPI")

fun Route.cartApi() {
    authenticate("jwt-auth") {
        route("/cart") {
            get {
                val username = JwtConfig.getUsername(call.principal<JWTPrincipal>()!!)
                val user = userService.findUserByUsername(username)
                call.respond(mapper.writeValueAsString(cartService.getContents(user.id)))
            }
            put {
                try {
                    val username = JwtConfig.getUsername(call.principal<JWTPrincipal>()!!)
                    val user = userService.findUserByUsername(username)

                    val requestData = call.receive<String>()
                    val parsedData = mapper.readTree(requestData)
                    val action = parsedData["action"].asText()
                    val book = mapper.readValue(parsedData["book"].toString(), Book::class.java)
                    handleBookAction(action, book, user.id)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    logger.error(e.message)
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            delete {
                val username = JwtConfig.getUsername(call.principal<JWTPrincipal>()!!)
                val user = userService.findUserByUsername(username)
                cartService.cleanCart(user.id)
                call.respond(HttpStatusCode.OK)
            }
            get("/checkout") {
                try {
                    val username = JwtConfig.getUsername(call.principal<JWTPrincipal>()!!)
                    val user = userService.findUserByUsername(username)
                    cartService.checkout(user.id)
                } catch (e: Exception) {
                    logger.error(e.message)
                    call.respond(HttpStatusCode.BadRequest)
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun handleBookAction(action: String, book: Book, userId: String) {
    if ("add".equals(action, ignoreCase = true)) {
        cartService.addBook(userId, book)
    } else if ("remove".equals(action, ignoreCase = true)) {
        cartService.removeBook(userId, book)
    } else {
        throw Exception("Invalid action")
    }
}
