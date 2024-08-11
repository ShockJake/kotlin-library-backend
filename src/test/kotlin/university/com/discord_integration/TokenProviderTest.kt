package university.com.discord_integration

import kotlin.test.Test
import kotlin.test.assertEquals


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