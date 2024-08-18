package university.com.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.util.logging.KtorSimpleLogger
import university.com.common.ServiceProvider.getPurchaseService
import university.com.common.ServiceProvider.getUserService
import university.com.data.model.Book
import university.com.security.JwtConfig

private val purchaseService = getPurchaseService()
private val userService = getUserService()
private val mapper = jacksonObjectMapper()
private val logger = KtorSimpleLogger("PurchaseAPI")

fun Route.purchaseApi() {
    authenticate("jwt-auth") {
        route("/purchase") {
            get {
                val username = JwtConfig.getUsername(call.principal<JWTPrincipal>()!!)
                val user = userService.findUserByUsername(username)
                call.respond(HttpStatusCode.OK, mapper.writeValueAsString(purchaseService.getPurchases(user.id)))
            }

            post {
                try {
                    val data = call.receive<String>()
                    val books = mapper.readValue<List<Book>>(data)

                    val username = JwtConfig.getUsername(call.principal<JWTPrincipal>()!!)
                    val user = userService.findUserByUsername(username)

                    purchaseService.addPurchase(user.id, books)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    logger.error("Cannot register purchase: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
