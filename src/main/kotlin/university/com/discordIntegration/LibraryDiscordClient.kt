package university.com.discordIntegration

import discord4j.core.DiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger

class LibraryDiscordClient(discordClient: DiscordClient, operationsService: OperationsService) {
    private val logger: Logger = KtorSimpleLogger(LibraryDiscordClient::class.java.simpleName)
    private val gateway = discordClient.login().block()

    init {
        val guilds = gateway?.guilds?.map { it.id.asLong() }?.collectList()?.block()
        val applicationId = gateway?.restClient?.applicationId?.block()
        if (applicationId != null) {
            operationsService.registerOperations(guilds, applicationId, gateway)
            gateway?.on(ChatInputInteractionEvent::class.java)?.subscribe { event ->
                operationsService.respondToInteractionEvent(event)
            }
            gateway?.on(ReadyEvent::class.java)?.subscribe { event ->
                logger.info("Logged as a ${event.self.username}")
            }
        } else {
            error("Error while logging in")
        }
    }

    fun logout() {
        logger.info("Stopping the Discord Client")
        gateway?.logout()?.block()
    }

    fun start() {
        logger.info("Starting the Discord Client")
    }
}
