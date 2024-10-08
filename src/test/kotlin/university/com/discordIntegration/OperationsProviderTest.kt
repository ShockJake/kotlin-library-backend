package university.com.discordIntegration

import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperationsProviderTest {
    @Test
    fun shouldProvideAllOperations() {
        // given
        val operationProvider = OperationsProvider()
        val operationExecutor = OperationExecutor(listOf("hello", "categories", "get_books_by_category"), mock())

        // when
        val operations = operationProvider.operations

        // then
        assertEquals(3, operations.size)
        assertTrue { operations.containsKey("hello") }
        assertTrue { operations.containsKey("categories") }
        assertTrue { operations.containsKey("get_books_by_category") }
        assertNotNull(operations["hello"])
        assertNotNull(operations["categories"])
        assertNotNull(operations["get_books_by_category"])
        assertEquals(operations["hello"]?.second?.javaClass?.name, operationExecutor.sayHello.javaClass.name)
        assertEquals(operations["categories"]?.second?.javaClass?.name, operationExecutor.getCategories.javaClass.name)
        assertEquals(
            operations["get_books_by_category"]?.second?.javaClass?.name,
            operationExecutor.getBooksByCategory.javaClass.name
        )
    }
}
