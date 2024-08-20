package university.com.discordIntegration

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.Interaction
import discord4j.core.`object`.entity.User
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import discord4j.discordjson.json.ImmutableApplicationCommandRequest
import discord4j.rest.RestClient
import discord4j.rest.service.ApplicationService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import kotlin.test.BeforeTest
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperationsServiceTest {
    private val operations = OperationsProvider().operations
    private val guilds = listOf(1L)
    private val applicationId = 1L
    private var gateway: GatewayDiscordClient = mock()
    private lateinit var operationsService: OperationsService

    @BeforeTest
    fun setUp() {
        gateway = mock()
        operationsService = OperationsService()
    }

    @Test
    fun shouldRegisterAllOperations() {
        // given
        val applicationService = setupMocksForRegistration()

        // when
        operationsService.registerOperations(guilds, applicationId, gateway)

        // then
        verify(applicationService).createGuildApplicationCommand(
            applicationId,
            guilds.first(),
            operations["hello"]!!.first
        )
        verify(applicationService).createGuildApplicationCommand(
            applicationId,
            guilds.first(),
            operations["categories"]!!.first
        )
        verify(applicationService).createGuildApplicationCommand(
            applicationId,
            guilds.first(),
            operations["get_books_by_category"]!!.first
        )
    }

    @Test
    fun shouldRespondToInteractionEvent() {
        // given
        setupMocksForRegistration()
        operationsService.registerOperations(guilds, applicationId, gateway)

        val expectedMessage = "Hi! I am a Librarian bot.\nHere are the commands:\n${
        operations.keys.asSequence().map { "- `/$it`" }.joinToString("\n")
        }"
        val interaction: Interaction = mock()
        val user: User = mock()
        whenever(interaction.user).thenReturn(user)
        whenever(user.username).thenReturn("TEST_USER")

        val event: ChatInputInteractionEvent = mock()
        whenever(event.commandName).thenReturn(operations.keys.first())
        whenever(event.interaction).thenReturn(interaction)
        whenever(event.reply(expectedMessage)).thenReturn(InteractionApplicationCommandCallbackReplyMono.of(event))

        // when
        operationsService.respondToInteractionEvent(event)

        // then
        runBlocking {
            withTimeout(3000) {
                verify(event).commandName
                verify(event).interaction
                verify(event).reply(eq(expectedMessage))
            }
        }
    }

    private fun setupMocksForRegistration(): ApplicationService {
        val restClient: RestClient = mock()
        val applicationService: ApplicationService = mock()
        whenever(gateway.restClient).thenReturn(restClient)
        whenever(restClient.applicationService).thenReturn(applicationService)
        whenever(
            applicationService.createGuildApplicationCommand(
                eq(applicationId),
                eq(guilds.first()),
                any<ImmutableApplicationCommandRequest>()
            )
        ).thenReturn(Mono.empty())

        return applicationService
    }
}
