package university.com

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.logging.KtorSimpleLogger
import university.com.common.DiscordClientProvider.getDiscordClient
import university.com.discordIntegration.LibraryDiscordClient
import university.com.discordIntegration.OperationsService
import university.com.plugins.configureHTTP
import university.com.plugins.configureRouting
import university.com.plugins.configureSecurity
import university.com.plugins.configureSerialization
import java.util.concurrent.TimeUnit

val logger = KtorSimpleLogger("com.university.ApplicationKt")

fun main() {
    setup()
    Thread.currentThread().join()
}

fun setup() {
    val shouldStartDiscordClient = getDiscordIntegrationEnabledProperty()
    val server = setupServer()
    val discordClient = setupDiscordClient(shouldStartDiscordClient)

    logger.info("Starting the Application")
    server.start(wait = false)

    Runtime.getRuntime().addShutdownHook(Thread { shutdown(discordClient, server) })
}

fun getDiscordIntegrationEnabledProperty(): Boolean {
    return System.getProperty("discord.integration.enabled", "false").toBoolean()
}

fun setupServer(): NettyApplicationEngine {
    val host = "0.0.0.0"
    val port = 8080
    logger.debug("Configuring HTTP server: $host:$port")
    return embeddedServer(Netty, port = port, host = host, module = Application::module)
}

fun setupDiscordClient(shouldRun: Boolean): LibraryDiscordClient? {
    if (!shouldRun) {
        return null
    }

    val discordClient = LibraryDiscordClient(getDiscordClient(), OperationsService())
    discordClient.start()
    return discordClient
}

fun shutdown(discordClient: LibraryDiscordClient?, server: NettyApplicationEngine) {
    logger.info("Shutting down start")
    if (getDiscordIntegrationEnabledProperty() && discordClient != null) {
        discordClient.logout()
    }
    logger.info("Stopping server")
    server.stop(10, 10, TimeUnit.SECONDS)
    logger.info("Shutting down end...")
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()
}
