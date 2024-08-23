package university.com.security

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import university.com.data.service.CartService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    private val username = "TEST_USER"
    private val password = "TEST_PASSWORD"

    private lateinit var userService: UserService
    private lateinit var cartService: CartService

    @BeforeTest
    fun setUp() {
        cartService = mock()
        userService = UserService(cartService)
    }

    @Test
    fun shouldRegisterUser() {
        // given
        val expectedUser = User("SOME_ID", username, password)

        // when
        val actualUser = userService.registerUser(username, password)

        // then
        assertEquals(expectedUser.username, actualUser.username)
        assertNotEquals(expectedUser.passwordHash, actualUser.passwordHash)
        verify(cartService).createCart(any())
    }

    @Test
    fun shouldThrowExceptionIfUserAlreadyExists() {
        // given
        userService.registerUser(username, password)

        // when & then
        assertThrows<IllegalStateException> {
            userService.registerUser(username, password)
        }
    }

    @Test
    fun shouldAuthenticateUser() {
        // given
        userService.registerUser(username, password)

        // when
        val result = userService.authenticateUser(username, password)

        // then
        assertTrue(result)
    }

    @Test
    fun shouldNotAuthenticateIfUserDoesNotExist() {
        // when
        val result = userService.authenticateUser(username, password)

        // then
        assertFalse(result)
    }

    @Test
    fun shouldFindUserByUsername() {
        // given
        val expectedUser = userService.registerUser(username, password)

        // when
        val actualUser = userService.findUserByUsername(username)

        // then
        assertEquals(expectedUser, actualUser)
    }

    @Test
    fun shouldThrowExceptionIfFindingUserThatDoesNotExist() {
        // when & then
        assertThrows<IllegalStateException> {
            userService.findUserByUsername(username)
        }
    }

    @Test
    fun shouldDeleteUser() {
        // given
        val expectedUser = userService.registerUser(username, password)

        // when & then
        assertDoesNotThrow { userService.deleteUser(expectedUser.id) }
        assertThrows<IllegalStateException> {
            userService.findUserByUsername(username)
        }
    }

    @Test
    fun shouldThrowExceptionIfDeletingUserThatDoesNotExist() {
        // when & then
        assertThrows<IllegalStateException> {
            userService.deleteUser("SOME_ID")
        }
    }
}
