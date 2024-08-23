package university.com.security

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.util.logging.KtorSimpleLogger
import university.com.data.service.CartService
import java.util.UUID

data class User(val id: String, val username: String, val passwordHash: String)

class UserService(private val cartService: CartService) {
    private val logger = KtorSimpleLogger(UserService::class.java.simpleName)
    private val users = mutableMapOf<String, User>()

    fun registerUser(username: String, password: String): User {
        logger.info("Registering user $username")
        if (users.values.any { it.username == username }) {
            error("User already exists")
        }
        val passwordHash = hashPassword(password)
        val id = UUID.randomUUID().toString()
        val user = User(id, username, passwordHash)
        users[id] = user
        cartService.createCart(id)
        return user
    }

    fun authenticateUser(username: String, password: String): Boolean {
        logger.debug("Authenticating user $username")
        val user = users.values.find { it.username == username }
        if (user == null) {
            return false
        }
        return verifyPassword(password, user.passwordHash)
    }

    fun findUserByUsername(username: String): User {
        logger.debug("Searching for user $username")
        val user = users.values.find { it.username == username }
        if (user == null) {
            error("User not found")
        }
        return user
    }

    fun deleteUser(userId: String) {
        logger.info("Deleting user $userId")
        if (users.remove(userId) == null) {
            error("User not found")
        }
    }

    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    private fun verifyPassword(rawPassword: String, passwordHash: String): Boolean {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), passwordHash).verified
    }
}
