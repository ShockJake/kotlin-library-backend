package university.com.discordIntegration

import discord4j.core.DiscordClient
import discord4j.rest.RestClient
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import university.com.MockSetup.setupDiscordClientMock
import university.com.common.DiscordClientProvider.setMockDiscordClient
import university.com.common.DiscordClientProvider.useMock
import kotlin.test.BeforeTest
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryDiscordClientTest {
    private lateinit var restClient: RestClient
    private lateinit var discordClient: DiscordClient
    private lateinit var operationsService: OperationsService

    @BeforeAll
    fun beforeAll() {
        useMock(true)
    }

    @BeforeTest
    fun setUp() {
        operationsService = mock()
        restClient = mock()
        discordClient = mock()
        setupDiscordClientMock(discordClient, restClient)
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
}
