package nebulosa.api.scheduler

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

abstract class ScheduledTask<T> : Runnable {

    @JsonIgnore private val running = AtomicBoolean(false)
    @JsonIgnore private val future = AtomicReference<Future<*>>(null)

    abstract val name: String

    @Volatile var progress = 0.0
        protected set

    @Volatile var startedAt: LocalDateTime? = null
        private set

    @Volatile var finishedAt: LocalDateTime? = null
        private set

    @Volatile var finishedWithError = false
        protected set

    protected abstract fun execute()

    internal fun attach(future: Future<*>) {
        this.future.set(future)
    }

    open fun finishGracefully() = Unit

    fun isRunning() = running.get()

    fun isCancelled() = future.get()?.isCancelled ?: false

    fun isDone() = future.get()?.isDone ?: false

    fun cancel(mayInterruptIfRunning: Boolean = true) {
        future.get()?.cancel(mayInterruptIfRunning)
    }

    final override fun run() {
        try {
            running.set(true)
            startedAt = LocalDateTime.now(ZoneOffset.UTC)
            execute()
        } finally {
            finishedAt = LocalDateTime.now(ZoneOffset.UTC)
            running.set(false)
            progress = 1.0
        }
    }
}
