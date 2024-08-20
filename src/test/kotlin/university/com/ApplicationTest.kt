package university.com

import discord4j.core.DiscordClient
import discord4j.discordjson.json.ImmutableApplicationCommandRequest
import discord4j.rest.RestClient
import discord4j.rest.service.ApplicationService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.stop
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import university.com.MockSetup.setupDiscordClientMock
import university.com.api.ApiTestCommons.setMockEngine
import university.com.common.DiscordClientProvider
import university.com.discordIntegration.LibraryDiscordClient
import university.com.plugins.configureRouting
import university.com.plugins.configureSecurity
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {
    @BeforeTest
    fun setUp() {
        setMockEngine()
    }

    @Test
    fun testRoot() = testApplication {
        application {
            configureSecurity()
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun shouldRunSetup() = testApplication {
        // when & then
        assertDoesNotThrow {
            setup()
        }
    }

    @Test
    fun shouldGetDefaultDiscordIntegrationEnabledProperty() = testApplication {
        // when & then
        assertEquals(false, getDiscordIntegrationEnabledProperty())
    }

    @Test
    fun shouldGetDefinedDiscordIntegrationEnabledProperty() = testApplication {
        // given
        System.setProperty("discord.integration.enabled", "true")

        // when
        val result = getDiscordIntegrationEnabledProperty()

        // then
        System.clearProperty("discord.integration.enabled")
        assertEquals(true, result)
    }

    @Test
    fun shouldSetupServer() = testApplication {
        // when & then
        assertDoesNotThrow {
            setupServer()
        }
    }

    @Test
    fun shouldNotStartDiscordClientIfIntegrationIsNotEnabled() = testApplication {
        // when & then
        assertDoesNotThrow {
            val result = setupDiscordClient(false)
            assertEquals(null, result)
        }
    }

    @Test
    fun shouldThrowExceptionWhenDiscordTokenIsNotCorrect() = testApplication {
        DiscordClientProvider.useMock(false)

        // when & then
        assertThrows<IllegalArgumentException> {
            setupDiscordClient(true)
        }
    }

    @Test
    fun shouldCreateLibraryDiscordClient() = testApplication {
        // given
        DiscordClientProvider.useMock(true)
        val restClient: RestClient = mock()
        val applicationService: ApplicationService = mock()
        val discordClient: DiscordClient = mock()
        val gateway = setupDiscordClientMock(discordClient, restClient)
        whenever(restClient.applicationService).thenReturn(applicationService)
        whenever(
            applicationService.createGuildApplicationCommand(
                eq(1),
                eq(1),
                any<ImmutableApplicationCommandRequest>()
            )
        ).thenReturn(Mono.empty())

        DiscordClientProvider.setMockDiscordClient(discordClient)

        // when
        val result = setupDiscordClient(true)

        // then
        assertNotNull(result)
    }

    @Test
    fun shouldShutdownProperly() = testApplication {
        // given
        System.setProperty("discord.integration.enabled", "true")
        val discordClient: LibraryDiscordClient = mock()
        val server: NettyApplicationEngine = mock()

        // when
        shutdown(discordClient, server)

        // then
        verify(discordClient).logout()
        verify(server).stop(10, 10, TimeUnit.SECONDS)
    }
}
