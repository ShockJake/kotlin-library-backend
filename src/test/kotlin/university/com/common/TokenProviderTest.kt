package university.com.common

import org.junit.jupiter.api.TestInstance
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenProviderTest {
    private val tokenProvider = TokenProvider

    @Test
    fun shouldParseDiscordToken() {
        // given
        val expectedToken = "TEST_TOKEN"

        // when
        val actualToken = tokenProvider.getDiscordToken()

        // then
        assertEquals(expectedToken, actualToken)
    }

    @Test
    fun shouldParseGoogleOauthData() {
        // given
        val expectedClientID = "GOOGLE_CLIENT_ID"
        val expectedSecret = "GOOGLE_SECRET"

        // when
        val googleOauthData = tokenProvider.getGoogleOAuthData()

        // then
        assertEquals(expectedClientID, googleOauthData["client_id"])
        assertEquals(expectedSecret, googleOauthData["secret"])
    }

    @Test
    fun shouldParseGitHubOauthData() {
        // given
        val expectedClientID = "GITHUB_CLIENT_ID"
        val expectedSecret = "GITHUB_SECRET"

        // when
        val githubOauthData = tokenProvider.getGithubOAuthData()

        // then
        assertEquals(expectedClientID, githubOauthData["client_id"])
        assertEquals(expectedSecret, githubOauthData["secret"])
    }
}
