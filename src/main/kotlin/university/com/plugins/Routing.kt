package university.com.plugins

import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import university.com.api.bookApi
import university.com.api.cartApi
import university.com.api.purchaseApi

fun Application.configureRouting() {
    install(Resources)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        purchaseApi()
        bookApi()
        cartApi()
    }
}
