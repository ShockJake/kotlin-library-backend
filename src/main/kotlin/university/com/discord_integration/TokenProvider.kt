package university.com.discord_integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.util.logging.*

class TokenProvider {
    private val objectMapper = jacksonObjectMapper()
    private val tokens: MutableMap<String, String> = HashMap()
    private val discordToken: String = "discord_token"
    private val logger: Logger = KtorSimpleLogger(TokenProvider::class.java.simpleName)

    init {
        try {
            logger.info("Initializing token util")
            val tokenData = this.javaClass.classLoader.getResource("tokens.json")?.readText()
            logger.debug("Token data: {}", tokenData)
            val node: JsonNode = objectMapper.readTree(tokenData)
            tokens[discordToken] = node.get(discordToken).textValue()
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun getDiscordToken(): String {
        return tokens[discordToken] ?: ""
    }
}
