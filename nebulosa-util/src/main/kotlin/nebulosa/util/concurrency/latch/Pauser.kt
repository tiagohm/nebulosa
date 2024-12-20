package nebulosa.util.concurrency.latch

import java.time.Duration
import java.util.concurrent.TimeUnit

open class Pauser : Pauseable, AutoCloseable {

    private val latch = CountUpDownLatch()
    private val listeners = LinkedHashSet<PauseListener>()

    final override val isPaused
        get() = !latch.get()

    @Synchronized
    fun listenToPause(listener: PauseListener) {
        listeners.add(listener)
    }

    @Synchronized
    fun unlistenToPause(listener: PauseListener) {
        listeners.remove(listener)
    }

    fun clearPauseListeners() {
        listeners.clear()
    }

    final override fun pause() {
        if (latch.get()) {
            latch.countUp(1)
            listeners.forEach { it.onPause(true) }
        }
    }

    final override fun unpause() {
        if (!latch.get()) {
            latch.reset()
            listeners.forEach { it.onPause(false) }
        }
    }

    override fun close() {
        unpause()
        clearPauseListeners()
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
