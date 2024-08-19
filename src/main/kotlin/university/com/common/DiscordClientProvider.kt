package university.com.common

import discord4j.core.DiscordClient
import university.com.discordIntegration.TokenProvider

object DiscordClientProvider {

    private var useMock = false
    private var mockDiscordClient: DiscordClient? = null

    fun setMockDiscordClient(mockDiscordClient: DiscordClient?) {
        DiscordClientProvider.mockDiscordClient = mockDiscordClient
    }

    fun useMock(useMock: Boolean) {
        DiscordClientProvider.useMock = useMock
    }

    fun getDiscordClient(): DiscordClient {
        if (useMock && mockDiscordClient != null) {
            mockDiscordClient?.let {
                return mockDiscordClient as DiscordClient
            }
        }
        return DiscordClient.builder(TokenProvider().getDiscordToken()).build()
    }
}
