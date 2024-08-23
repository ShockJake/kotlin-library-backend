package university.com.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout

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
                return HttpClient(mockEngine!!)
            }
        }
        return HttpClient(CIO) { install(HttpTimeout) { requestTimeoutMillis = 30000 } }
    }
}
