package university.com.security

import io.ktor.http.HttpMethod
import org.junit.jupiter.api.TestInstance
import university.com.common.TokenProvider.getGithubOAuthData
import university.com.common.TokenProvider.getGoogleOAuthData
import university.com.security.OAuthSecurityConfiguration.getGitHubOAuthConfiguration
import university.com.security.OAuthSecurityConfiguration.getGoogleOAuthConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OAuthSecurityConfigurationTest {

    @Test
    fun shouldGetGoogleOauthConfiguration() {
        // given
        val googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
        val googleTokenUrl = "https://accounts.google.com/o/oauth2/token"
        val googleOAuthData = getGoogleOAuthData()
        val redirectUrls = mutableMapOf<String, String>()
        val scopes = listOf(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
        )
        val authParams = listOf("access_type" to "offline")

        // when
        val configuration = getGoogleOAuthConfiguration(redirectUrls)

        // then
        assertEquals("google", configuration.name)
        assertEquals(googleOAuthData["client_id"], configuration.clientId)
        assertEquals(googleOAuthData["secret"], configuration.clientSecret)
        assertEquals(googleAuthUrl, configuration.authorizeUrl)
        assertEquals(googleTokenUrl, configuration.accessTokenUrl)
        assertEquals(HttpMethod.Post, configuration.requestMethod)
        assertEquals(scopes, configuration.defaultScopes)
        assertEquals(authParams, configuration.extraAuthParameters)
    }

    @Test
    fun shouldGetGitHubOauthConfiguration() {
        // given
        val githubAuthUrl = "https://github.com/login/oauth/authorize"
        val githubTokenUrl = "https://github.com/login/oauth/access_token"
        val githubOAuthData = getGithubOAuthData()
        val redirectUrls = mutableMapOf<String, String>()
        val scopes = listOf("read:user", "user:email")

        // when
        val configuration = getGitHubOAuthConfiguration(redirectUrls)

        // then
        assertEquals("github", configuration.name)
        assertEquals(githubOAuthData["client_id"], configuration.clientId)
        assertEquals(githubOAuthData["secret"], configuration.clientSecret)
        assertEquals(githubAuthUrl, configuration.authorizeUrl)
        assertEquals(githubTokenUrl, configuration.accessTokenUrl)
        assertEquals(HttpMethod.Post, configuration.requestMethod)
        assertEquals(scopes, configuration.defaultScopes)
    }
}
