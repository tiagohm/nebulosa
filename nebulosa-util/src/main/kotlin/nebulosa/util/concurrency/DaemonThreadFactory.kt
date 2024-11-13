package nebulosa.util.concurrency

import nebulosa.log.di
import nebulosa.log.loggerFor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

data class DaemonThreadFactory(private val name: String) : ThreadFactory {

    private val counter = AtomicInteger(1)

    override fun newThread(task: Runnable): Thread {
        val thread = Thread(task, "$name Thread ${counter.getAndIncrement()}")
        thread.isDaemon = true
        LOG.di("new thread. name={}, id={}", thread.name, thread.id)
        return thread
    }

    companion object {

        private val LOG = loggerFor<DaemonThreadFactory>()
    }
}
