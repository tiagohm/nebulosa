package nebulosa.common.concurrency

import java.io.Closeable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class CancellationToken : Closeable, Future<Boolean> {

    private val latch = CountUpDownLatch(1)
    private val listeners = LinkedHashSet<Runnable>()

    fun listen(action: Runnable): Boolean {
        return if (isDone) {
            action.run()
            false
        } else {
            listeners.add(action)
        }
    }

    fun cancel() {
        cancel(true)
    }

    @Synchronized
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (latch.count <= 0) return false
        latch.reset()
        listeners.forEach(Runnable::run)
        listeners.clear()
        return true
    }

    override fun isCancelled(): Boolean {
        return latch.get()
    }

    override fun isDone(): Boolean {
        return latch.get()
    }

    override fun get(): Boolean {
        latch.await()
        return true
    }

    override fun get(timeout: Long, unit: TimeUnit): Boolean {
        return latch.await(timeout, unit)
    }

    fun reset() {
        latch.countUp(1 - latch.count)
        listeners.clear()
    }

    override fun close() {
        latch.reset()
        listeners.clear()
    }
}
