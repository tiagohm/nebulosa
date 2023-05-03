package nebulosa.desktop.logic

import kotlinx.coroutines.*
import nebulosa.desktop.service.PreferenceService
import org.springframework.beans.factory.annotation.Autowired
import java.io.Closeable

abstract class AbstractManager : Closeable, CoroutineScope {

    @Autowired protected lateinit var preferenceService: PreferenceService

    private val job = SupervisorJob()

    override val coroutineContext = job + Dispatchers.IO

    override fun close() {
        job.cancel()
    }

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun launch(noinline block: suspend CoroutineScope.() -> Unit): Job {
        return launch(Dispatchers.IO, block = block)
    }
}
