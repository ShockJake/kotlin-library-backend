package university.com.discordIntegration

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import discord4j.rest.RestClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import university.com.common.DiscordClientProvider.setMockDiscordClient
import university.com.common.DiscordClientProvider.useMock
import kotlin.test.BeforeTest
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryDiscordClientTest {
    private lateinit var discordClient: DiscordClient
    private lateinit var operationsService: OperationsService

    @BeforeAll
    fun beforeAll() {
        useMock(true)
    }

    @BeforeTest
    fun setUp() {
        operationsService = mock()
        discordClient = mock()
        setupMock()
        setMockDiscordClient(discordClient)
    }

    @Test
    fun shouldRegisterIntegration() {
        // when & then
        assertDoesNotThrow {
            LibraryDiscordClient(discordClient, operationsService)
        }
    }

    @Test
    fun shouldLogout() {
        // given
        val libraryDiscordClient = LibraryDiscordClient(discordClient, operationsService)

        // when
        assertDoesNotThrow {
            libraryDiscordClient.logout()
        }
    }

    @Test
    fun shouldStart() {
        // given
        val libraryDiscordClient = LibraryDiscordClient(discordClient, operationsService)

        // when & then
        assertDoesNotThrow {
            libraryDiscordClient.start()
        }
    }

    private fun setupMock() {
        val guild: Guild = mock()
        whenever(guild.id).thenReturn(Snowflake.of(1))

        val restClient: RestClient = mock()
        whenever(restClient.applicationId).thenReturn(Mono.fromCallable { 1 })

        val gateway: GatewayDiscordClient = mock()
        whenever(gateway.guilds).thenReturn(Flux.just(guild))

        val inputEvent: ChatInputInteractionEvent = mock()
        val readyEvent: ReadyEvent = mock()
        val user: User = mock()
        whenever(user.username).thenReturn("TEST")
        whenever(readyEvent.self).thenReturn(user)
        whenever(gateway.on(ChatInputInteractionEvent::class.java)).thenReturn(Flux.just(inputEvent))
        whenever(gateway.on(ReadyEvent::class.java)).thenReturn(Flux.just(readyEvent))
        whenever(gateway.restClient).thenReturn(restClient)
        whenever(discordClient.login()).thenReturn(Mono.fromCallable { gateway })
        whenever(gateway.logout()).thenReturn(Mono.empty())
    }
}
