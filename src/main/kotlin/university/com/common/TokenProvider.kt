package university.com.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger

object TokenProvider {
    private val objectMapper = jacksonObjectMapper()
    private val tokens: MutableMap<String, String> = HashMap()
    private const val DISCORD_TOKEN_PROPERTY: String = "discord_token"
    private const val GOOGLE_OAUTH_CLIENT_ID_PROPERTY: String = "google_oauth_client_id"
    private const val GOOGLE_OAUTH_SECRET_PROPERTY: String = "google_oauth_client_secret"
    private const val GITHUB_OAUTH_CLIENT_ID_PROPERTY: String = "github_oauth_client_id"
    private const val GITHUB_OAUTH_SECRET_PROPERTY: String = "github_oauth_client_secret"
    private val logger: Logger = KtorSimpleLogger(TokenProvider::class.java.simpleName)

    init {
        try {
            logger.info("Initializing token provider")
            val tokenData = this.javaClass.classLoader.getResource("tokens.json")?.readText()
            val node: JsonNode = objectMapper.readTree(tokenData)
            tokens[DISCORD_TOKEN_PROPERTY] = node[DISCORD_TOKEN_PROPERTY].textValue()
            tokens[GOOGLE_OAUTH_CLIENT_ID_PROPERTY] = node[GOOGLE_OAUTH_CLIENT_ID_PROPERTY].textValue()
            tokens[GOOGLE_OAUTH_SECRET_PROPERTY] = node[GOOGLE_OAUTH_SECRET_PROPERTY].textValue()
            tokens[GITHUB_OAUTH_CLIENT_ID_PROPERTY] = node[GITHUB_OAUTH_CLIENT_ID_PROPERTY].textValue()
            tokens[GITHUB_OAUTH_SECRET_PROPERTY] = node[GITHUB_OAUTH_SECRET_PROPERTY].textValue()

            logger.debug("Parsed next tokens: {}", tokens.keys)
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun getDiscordToken(): String {
        return tokens[DISCORD_TOKEN_PROPERTY] ?: ""
    }

    fun getGoogleOAuthData(): Map<String, String> {
        return mapOf(
            "client_id" to tokens[GOOGLE_OAUTH_CLIENT_ID_PROPERTY]!!,
            "secret" to tokens[GOOGLE_OAUTH_SECRET_PROPERTY]!!
        )
    }

    fun getGithubOAuthData(): Map<String, String> {
        return mapOf(
            "client_id" to tokens[GITHUB_OAUTH_CLIENT_ID_PROPERTY]!!,
            "secret" to tokens[GITHUB_OAUTH_SECRET_PROPERTY]!!
        )
    }
}
