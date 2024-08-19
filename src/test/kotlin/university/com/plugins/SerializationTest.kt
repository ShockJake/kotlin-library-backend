package university.com.plugins

import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SerializationTest {

    @Test
    fun shouldConfigureSerialization() = testApplication {
        // when & then
        assertDoesNotThrow {
            application { configureSerialization() }
        }
    }
}
