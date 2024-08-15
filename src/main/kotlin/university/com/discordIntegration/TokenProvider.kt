package university.com.discordIntegration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger

class TokenProvider {
    private val objectMapper = jacksonObjectMapper()
    private val tokens: MutableMap<String, String> = HashMap()
    private val discordTokenPropertyName: String = "discord_token"
    private val logger: Logger = KtorSimpleLogger(TokenProvider::class.java.simpleName)

    init {
        try {
            logger.info("Initializing token util")
            val tokenData = this.javaClass.classLoader.getResource("tokens.json")?.readText()
            logger.debug("Token data: {}", tokenData)
            val node: JsonNode = objectMapper.readTree(tokenData)
            tokens[discordTokenPropertyName] = node.get(discordTokenPropertyName).textValue()
        } catch (e: Exception) {
            logger.error(e.message)
            throw e
        }
    }

    fun getDiscordToken(): String {
        return tokens[discordTokenPropertyName] ?: ""
    }
}
