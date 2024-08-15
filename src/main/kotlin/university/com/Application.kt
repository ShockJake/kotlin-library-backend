package university.com

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.util.logging.KtorSimpleLogger
import university.com.discordIntegration.LibraryDiscordClient
import university.com.plugins.configureHTTP
import university.com.plugins.configureRouting
import university.com.plugins.configureSecurity
import university.com.plugins.configureSerialization
import university.com.plugins.jsonModule
import java.util.concurrent.TimeUnit

fun main() {
    val logger = KtorSimpleLogger("com.university.ApplicationKt")
    logger.info("Starting the Application")
    val client = LibraryDiscordClient()
    client.start()

    val server = embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
    server.start(wait = false)

    Runtime.getRuntime()
        .addShutdownHook(
            Thread {
                logger.info("Shutting down start")
                client.logout()
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
