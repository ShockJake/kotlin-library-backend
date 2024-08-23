package university.com

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import discord4j.rest.RestClient
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object MockSetup {

    fun setupDiscordClientMock(discordClient: DiscordClient, restClient: RestClient): GatewayDiscordClient {
        val guild: Guild = mock()
        whenever(guild.id).thenReturn(Snowflake.of(1))

        whenever(restClient.applicationId).thenReturn(Mono.fromCallable { 1 })

        val gateway: GatewayDiscordClient = mock()
        whenever(gateway.guilds).thenReturn(Flux.just(guild))
        whenever(gateway.restClient).thenReturn(restClient)

        val inputEvent: ChatInputInteractionEvent = mock()
        val readyEvent: ReadyEvent = mock()
        val user: User = mock()
        whenever(user.username).thenReturn("TEST")
        whenever(readyEvent.self).thenReturn(user)
        whenever(gateway.on(ChatInputInteractionEvent::class.java)).thenReturn(Flux.just(inputEvent))
        whenever(gateway.on(ReadyEvent::class.java)).thenReturn(Flux.just(readyEvent))
        whenever(discordClient.login()).thenReturn(Mono.fromCallable { gateway })
        whenever(gateway.logout()).thenReturn(Mono.empty())

        return gateway
    }
}
