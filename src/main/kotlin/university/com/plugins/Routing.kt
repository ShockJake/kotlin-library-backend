package university.com.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
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
