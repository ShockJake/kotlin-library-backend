package university.com.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.Date

object JwtConfig {
    private const val SECRET = "jwt-secret"
    private const val ISSUER = "localhost"
    private const val AUDIENCE = "users"
    private const val VALIDITY_IN_MS = 36_000_00 * 2 // = 2h

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(username: String): String = JWT.create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_IN_MS))
        .sign(algorithm)

    fun getUsername(principal: JWTPrincipal): String {
        return principal.payload.getClaim("username").asString()
    }
}
