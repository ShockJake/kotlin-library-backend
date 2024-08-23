package university.com.security

import io.ktor.http.HttpMethod
import io.ktor.server.auth.OAuthServerSettings.OAuth2ServerSettings
import university.com.common.TokenProvider.getGithubOAuthData
import university.com.common.TokenProvider.getGoogleOAuthData

object OAuthSecurityConfiguration {
    private val googleOauthData = getGoogleOAuthData()
    private val githubOauthData = getGithubOAuthData()
    private const val GOOGLE_OAUTH_AUTH_URL = "https://accounts.google.com/o/oauth2/auth"

    fun getGoogleOAuthConfiguration(redirectUrls: MutableMap<String, String>): OAuth2ServerSettings {
        return OAuth2ServerSettings(
            name = "google",
            authorizeUrl = GOOGLE_OAUTH_AUTH_URL,
            accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
            requestMethod = HttpMethod.Post,
            clientId = googleOauthData["client_id"]!!,
            clientSecret = googleOauthData["secret"]!!,
            defaultScopes = listOf(
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/userinfo.email"
            ),
            extraAuthParameters = listOf("access_type" to "offline"),
            onStateCreated = { call, state ->
                call.request.queryParameters["redirectUrl"]?.let {
                    redirectUrls[state] = it
                }
            }
        )
    }

    fun getGitHubOAuthConfiguration(redirectUrls: MutableMap<String, String>): OAuth2ServerSettings {
        return OAuth2ServerSettings(
            name = "github",
            authorizeUrl = "https://github.com/login/oauth/authorize",
            accessTokenUrl = "https://github.com/login/oauth/access_token",
            clientId = githubOauthData["client_id"]!!,
            clientSecret = githubOauthData["secret"]!!,
            requestMethod = HttpMethod.Post,
            defaultScopes = listOf("read:user", "user:email"),
            onStateCreated = { call, state ->
                call.request.queryParameters["redirectUrl"]?.let {
                    redirectUrls[state] = it
                }
            }
        )
    }
}
