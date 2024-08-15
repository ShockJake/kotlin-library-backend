package university.com.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object DispatcherProvider {
    private val ioDispatcher = Dispatchers.IO

    fun getIO(): CoroutineDispatcher = ioDispatcher
}
