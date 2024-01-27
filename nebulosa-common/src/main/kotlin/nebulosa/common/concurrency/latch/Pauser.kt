package nebulosa.common.concurrency.latch

import java.io.Closeable
import java.time.Duration
import java.util.concurrent.TimeUnit

class Pauser : Closeable {

    private val latch = CountUpDownLatch()

    val isPaused
        get() = !latch.get()

    fun pause() {
        if (latch.get()) {
            latch.countUp(1)
        }
    }

    fun unpause() {
        if (!latch.get()) {
            latch.reset()
        }
    }

    override fun close() {
        unpause()
    }

    fun waitWhileIsPaused() {
        latch.await()
    }

    fun waitWhileIsPaused(timeout: Long, unit: TimeUnit): Boolean {
        return latch.await(timeout, unit)
    }

    fun waitWhileIsPaused(timeout: Duration): Boolean {
        return latch.await(timeout)
    }
}
