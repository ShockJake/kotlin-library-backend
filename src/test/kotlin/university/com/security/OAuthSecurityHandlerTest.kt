package university.com.security

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2
import io.ktor.server.auth.authentication
import io.ktor.server.response.ApplicationResponse
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.server.response.ResponseCookies
import io.ktor.server.response.ResponseHeaders
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import university.com.common.HttpClientProvider
import university.com.common.ObjectMapperProvider.getObjectMapper
import kotlin.test.BeforeTest
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OAuthSecurityHandlerTest {
    private val objectMapper = getObjectMapper()

    private lateinit var userService: UserService
    private lateinit var oAuthSecurityHandler: OAuthSecurityHandler

    private val googleUser = GoogleUserInfo(
        "test_id",
        "y",
        true,
        "test_name",
        "test_given_name",
        "test_family_name",
        "test_picture"
    )
    private val googleMockResponse = objectMapper.writeValueAsString(googleUser)

    @BeforeAll
    fun beforeAll() {
        HttpClientProvider.setUseMock(true)
        HttpClientProvider.setMockEngine(getMockedHttpEngine())
    }

    @AfterAll
    fun afterAll() {
        HttpClientProvider.setUseMock(false)
    }

    @BeforeTest
    fun setUp() {
        userService = mock()
        oAuthSecurityHandler = OAuthSecurityHandler(objectMapper, userService)
    }

    @Test
    fun shouldHandleGoogleOAuthAuthenticationSuccessfully() {
        // given
        val userName = "${googleUser.email}.oauth2"
        val source = "google"
        val state = "TEST"
        val redirectUrl = "http://test_url"
        val call: ApplicationCall = mock()
        val principal: OAuth2 = mock()
        val headers: ResponseHeaders = mock()
        val cookies: ResponseCookies = mock()
        val pipeline: ApplicationSendPipeline = mock()
        setupMocks(state, principal, cookies, call, userName, headers, pipeline)

        // when
        runBlocking {
            oAuthSecurityHandler.handleOAuthAuthenticatedUser(call, mutableMapOf(state to redirectUrl), source)
        }

        // then
        verify(principal, times(1)).state
        verify(principal, times(1)).accessToken
        verify(userService, times(1)).findUserByUsername(eq(userName))
        verify(userService, times(0)).registerUser(eq(userName), any())
        verify(cookies, times(1)).append(any())
        verify(headers).append(HttpHeaders.Location, redirectUrl)
        runBlocking {
            verify(pipeline, times(2)).execute(eq(call), any())
        }
    }

    @Test
    fun shouldHandleGitHubOAuthAuthenticationSuccessfully() {
        // given
        val userName = "testLogin.oauth2"
        val source = "github"
        val state = "TEST"
        val redirectUrl = "http://test_url"
        val call: ApplicationCall = mock()
        val principal: OAuth2 = mock()
        val headers: ResponseHeaders = mock()
        val cookies: ResponseCookies = mock()
        val pipeline: ApplicationSendPipeline = mock()
        setupMocks(state, principal, cookies, call, userName, headers, pipeline)

        // when
        runBlocking {
            oAuthSecurityHandler.handleOAuthAuthenticatedUser(call, mutableMapOf(state to redirectUrl), source)
        }

        // then
        verify(principal, times(1)).state
        verify(principal, times(1)).accessToken
        verify(userService, times(1)).findUserByUsername(eq(userName))
        verify(userService, times(0)).registerUser(eq(userName), any())
        verify(cookies, times(1)).append(any())
        verify(headers).append(HttpHeaders.Location, redirectUrl)
        runBlocking {
            verify(pipeline, times(2)).execute(eq(call), any())
        }
    }

    private fun setupMocks(
        state: String,
        principal: OAuth2,
        cookies: ResponseCookies,
        call: ApplicationCall,
        userName: String,
        headers: ResponseHeaders,
        pipeline: ApplicationSendPipeline
    ) {
        val accessToken = "TEST_ACCESS_TOKEN"
        val attributes: Attributes = mock()
        val authentication: AuthenticationContext = mock()
        val applicationResponse: ApplicationResponse = mock()
        whenever(attributes.getOrNull(AttributeKey<AuthenticationContext>("AuthContext"))).thenReturn(mock())
        whenever(attributes.getOrNull(AttributeKey<TypeInfo>("ResponseTypeAttributeKey"))).thenReturn(mock())
        whenever(principal.state).thenReturn(state)
        whenever(principal.accessToken).thenReturn(accessToken)
        whenever(authentication.principal<OAuth2>()).thenReturn(principal)
        whenever(applicationResponse.cookies).thenReturn(cookies)
        whenever(applicationResponse.call).thenReturn(call)
        whenever(applicationResponse.pipeline).thenReturn(pipeline)
        whenever(applicationResponse.headers).thenReturn(headers)
        whenever(call.response).thenReturn(applicationResponse)
        whenever(call.attributes).thenReturn(attributes)
        whenever(call.authentication).thenReturn(authentication)
        whenever(userService.findUserByUsername(userName)).thenReturn(getUser(userName))
    }

    private fun getUser(userName: String): User {
        return User("TEST_ID", userName, "SOME_PASSWORD")
    }

    private fun getMockedHttpEngine(): MockEngine {
        return MockEngine { request ->
            when (request.url.fullPath) {
                "/oauth2/v2/userinfo" -> respond(
                    content = googleMockResponse,
                    status = HttpStatusCode.OK
                )

                "/user" -> respond(
                    content = """{ "login" : "testLogin" }""",
                    status = HttpStatusCode.OK
                )

                else -> {
                    println(request.url)
                    println(request.url.fullPath)
                    respond(
                        content = """{"error": "Not Found"} """,
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }
    }
}
