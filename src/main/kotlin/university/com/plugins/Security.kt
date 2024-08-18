package university.com.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.logging.KtorSimpleLogger
import university.com.common.ObjectMapperProvider.getObjectMapper
import university.com.common.ServiceProvider.getUserService
import university.com.security.JwtConfig

val userService = getUserService()
val objectMapper = getObjectMapper()
val logger = KtorSimpleLogger("SecurityModule")

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("jwt-auth") {
            realm = "ktor.io"
            verifier(JwtConfig.verifier)
            validate {
                val payload = it.payload
                if (payload.getClaim("username").asString() != null) {
                    JWTPrincipal(payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    routing {
        post("/login") {
            val user = call.receive<UserPasswordCredential>()
            logger.debug("Authenticating user ${user.name}")
            if (userService.authenticateUser(user.name, user.password)) {
                val token = JwtConfig.generateToken(user.name)
                call.respond(objectMapper.writeValueAsString(mapOf("token" to token)))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
            }
        }
        post("/register") {
            val user = call.receive<UserPasswordCredential>()
            logger.info("Registering user ${user.name}")
            try {
                userService.registerUser(user.name, user.password)
                call.respond(HttpStatusCode.OK, """{"message": "User ${user.name} is created"}""")
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest, """{"errorMessage": "User ${user.name} already exists"}""")
            }
        }
    }
}
