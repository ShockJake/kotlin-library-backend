package university.com.discordIntegration

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OperationsService {
    private val logger = KtorSimpleLogger(OperationsProvider::class.java.simpleName)
    private val operations = OperationsProvider().operations

    fun registerOperations(guildIds: MutableList<Long>?, applicationId: Long, gateway: GatewayDiscordClient?) {
        logger.info("Registering operations for application $applicationId")
        val applicationService = gateway!!.restClient.applicationService
        guildIds?.forEach { guildId ->
            operations.forEach { (_, pair) ->
                applicationService.createGuildApplicationCommand(applicationId, guildId, pair.first).subscribe()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun respondToInteractionEvent(event: ChatInputInteractionEvent) {
        GlobalScope.launch {
            val operationName = event.commandName
            if (operations.keys.contains(operationName)) {
                logger.info("Executing operation: $operationName invoked by 'user ${event.interaction.user.username}'")
                operations[operationName]?.second?.invoke(event)
            } else {
                event.reply("Cannot find operation: $operationName").block()
            }
        }
    }
}
