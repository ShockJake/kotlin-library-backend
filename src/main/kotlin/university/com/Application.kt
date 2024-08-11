package university.com

import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.logging.*
import university.com.common.HttpClientProvider
import university.com.plugins.*
import java.util.concurrent.TimeUnit

fun main() {
    val logger = KtorSimpleLogger("com.university.ApplicationKt")
    logger.info("Starting the Application")
    //    val client = LibraryDiscordClient()
    //    client.start()

    val server = embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
    server.start(wait = false)

    Runtime.getRuntime()
        .addShutdownHook(
            Thread {
                logger.info("Shutting down start")
                //        client.logout()
                logger.info("Stopping server")
                server.stop(10, 10, TimeUnit.SECONDS)
                logger.info("Shutting down end...")
            }
        )
    Thread.currentThread().join()
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
    jsonModule()
}
