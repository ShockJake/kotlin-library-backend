package university.com.common

import discord4j.core.DiscordClient
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DiscordClientProviderTest {

    @Test
    fun shouldThrowExceptionWhenBuildingClient() {
        // given
        DiscordClientProvider.useMock(false)

        // when & then
        assertThrows<Exception> {
            DiscordClientProvider.getDiscordClient()
        }
    }

    @Test
    fun shouldCreateNormalClientIfMockNotProvided() {
        // given
        DiscordClientProvider.useMock(true)
        DiscordClientProvider.setMockDiscordClient(null)

        // when & then
        assertThrows<Exception> {
            DiscordClientProvider.getDiscordClient()
        }
    }

    @Test
    fun shouldCreateMockedClient() {
        // given
        val discordClient: DiscordClient = mock()
        DiscordClientProvider.useMock(true)
        DiscordClientProvider.setMockDiscordClient(discordClient)

        // when
        assertEquals(discordClient, DiscordClientProvider.getDiscordClient())
    }
}
