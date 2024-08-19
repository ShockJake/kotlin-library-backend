package university.com

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import university.com.api.ApiTestCommons.setMockEngine
import university.com.plugins.configureRouting
import university.com.plugins.configureSecurity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {
    @BeforeTest
    fun setUp() {
        setMockEngine()
    }

    @Test
    fun testRoot() = testApplication {
        application {
            configureSecurity()
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun shouldRunSetup() = testApplication {
        // when & then
        assertDoesNotThrow {
            setup()
        }
    }

    @Test
    fun shouldGetDefaultDiscordIntegrationEnabledProperty() = testApplication {
        // when & then
        assertEquals(false, getDiscordIntegrationEnabledProperty())
    }

    @Test
    fun shouldGetDefinedDiscordIntegrationEnabledProperty() = testApplication {
        // given
        System.setProperty("discord.integration.enabled", "true")

        // when
        val result = getDiscordIntegrationEnabledProperty()

        // then
        System.clearProperty("discord.integration.enabled")
        assertEquals(true, result)
    }

    @Test
    fun shouldSetupServer() = testApplication {
        // when & then
        assertDoesNotThrow {
            setupServer()
        }
    }

    @Test
    fun shouldNotStartDiscordClientIfIntegrationIsNotEnabled() = testApplication {
        // when & then
        assertDoesNotThrow {
            val result = setupDiscordClient(false)
            assertEquals(null, result)
        }
    }

    @Test
    fun shouldThrowExceptionWhenDiscordTokenIsNotCorrect() = testApplication {
        // when & then
        assertThrows<IllegalArgumentException> {
            setupDiscordClient(true)
        }
    }
}
