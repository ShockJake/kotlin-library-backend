package university.com.common

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*

object HttpClientProvider {

    private var useMock = false
    private var mockEngine: HttpClientEngine? = null

    fun setMockEngine(engine: HttpClientEngine) {
        this.mockEngine = engine
    }

    fun setUseMock(useMock: Boolean) {
        this.useMock = useMock
    }

    fun getClient(): HttpClient {
        if (useMock || mockEngine != null) {
            mockEngine?.let {
                return HttpClient(mockEngine!!) { install(HttpTimeout) { requestTimeoutMillis = 30000 } }
            }
        }
        return HttpClient(CIO) { install(HttpTimeout) { requestTimeoutMillis = 30000 } }
    }
}