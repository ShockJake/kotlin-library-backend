package university.com.discordIntegration

import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenProviderTest {

    @Test
    fun shouldParseToken() {
        // given
        val tokenProvider = TokenProvider()
        val expectedToken = "TEST_TOKEN"

        // when
        val actualToken = tokenProvider.getDiscordToken()

        // then
        assertEquals(expectedToken, actualToken)
    }
}
