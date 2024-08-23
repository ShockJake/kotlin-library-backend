package university.com.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.util.date.GMTDate
import io.ktor.util.date.plus
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.Serializable
import university.com.common.HttpClientProvider.getClient
import university.com.security.JwtConfig.generateToken
import java.util.UUID.randomUUID
import kotlin.time.Duration.Companion.hours

@Serializable
data class GoogleUserInfo(
    val id: String,
    val email: String,
    val verified_email: Boolean,
    val name: String,
    val given_name: String,
    val family_name: String,
    val picture: String
)

class OAuthSecurityHandler(private val objectMapper: ObjectMapper, private val userService: UserService) {
    val googleSource = "google"
    val githubSource = "github"

    private val logger = KtorSimpleLogger(OAuthSecurityHandler::class.java.simpleName)
    private val httpClient = getClient()

    suspend fun handleOAuthAuthenticatedUser(
        call: ApplicationCall,
        redirectUrls: MutableMap<String, String>,
        source: String
    ) {
        val principal = call.authentication.principal<OAuth2>()
        try {
            principal?.let {
                principal.state?.let { state ->
                    val accessToken = principal.accessToken
                    val userName = fetchUserName(source, accessToken)
                    val user = findOrRegisterUser(userName)
                    val token = generateToken(user.username)
                    call.response.cookies.append(getCookie(token))
                    val url = redirectUrls[state]
                    if (url != null) {
                        call.respondRedirect(url)
                        return
                    }
                }
            }
        } finally {
            call.respond(HttpStatusCode.Unauthorized, "Authentication Failed")
        }
    }

    private suspend fun fetchUserName(source: String, accessToken: String): String {
        logger.debug("Fetching user info from $source")
        val userName = when (source) {
            "google" -> fetchGoogleUserInfo(accessToken)
            "github" -> fetchGihHubUserEmail(accessToken)
            else -> error("Invalid source")
        }
        return "$userName.oauth2"
    }

    private suspend fun fetchGoogleUserInfo(accessToken: String): String {
        val response = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
            header("Authorization", "Bearer $accessToken")
        }
        if (HttpStatusCode.OK != response.status) {
            error("Cannot fetch user data")
        }
        return objectMapper.readValue<GoogleUserInfo>(response.bodyAsText()).email
    }

    private suspend fun fetchGihHubUserEmail(accessToken: String): String {
        val response = httpClient.get("https://api.github.com/user") {
            header("Authorization", "Bearer $accessToken")
        }
        if (HttpStatusCode.OK != response.status) {
            error("Cannot fetch user data")
        }
        return objectMapper.readTree(response.bodyAsText())["login"].asText()
    }

    private fun getCookie(token: String): Cookie {
        logger.debug("Providing cookie")
        return Cookie(
            name = "OAUTH_AUTHENTICATED_TOKEN",
            value = token,
            expires = GMTDate().plus(5.hours),
            maxAge = 0,
            secure = true,
            path = "/"
        )
    }

    private fun findOrRegisterUser(userName: String): User {
        return try {
            userService.findUserByUsername(userName)
        } catch (_: IllegalStateException) {
            userService.registerUser(userName, randomUUID().toString())
        }
    }
}
