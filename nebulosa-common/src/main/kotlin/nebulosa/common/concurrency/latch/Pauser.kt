package nebulosa.common.concurrency.latch

import java.io.Closeable
import java.time.Duration
import java.util.concurrent.TimeUnit

open class Pauser : Pauseable, Closeable {

    private val latch = CountUpDownLatch()

    final override val isPaused
        get() = !latch.get()

    final override fun pause() {
        if (latch.get()) {
            latch.countUp(1)
        }
    }

    final override fun unpause() {
        if (!latch.get()) {
            latch.reset()
        }
    }

    override fun close() {
        unpause()
    }

    fun waitForPause() {
        latch.await()
    }

    fun waitForPause(timeout: Long, unit: TimeUnit): Boolean {
        return latch.await(timeout, unit)
    }

    fun waitForPause(timeout: Duration): Boolean {
        return latch.await(timeout)
    }
}
