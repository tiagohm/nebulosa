package nebulosa.api.confirmation

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ConfirmationLatch : AutoCloseable {

    private val confirmed = AtomicBoolean(false)
    private val accepted = AtomicBoolean(false)
    private val latch = CountDownLatch(1)

    val isConfirmed
        get() = confirmed.get()

    val isAccepted
        get() = accepted.get()

    fun confirm(response: Boolean) {
        if (confirmed.compareAndSet(false, true)) {
            accepted.set(response)
            latch.countDown()
        }
    }

    fun waitForConfirmation(): Boolean {
        return latch.await().let { accepted.get() }
    }

    fun waitForConfirmation(timeout: Long, unit: TimeUnit): Boolean {
        return latch.await(timeout, unit) && accepted.get()
    }

    override fun close() {
        if (latch.count > 0) {
            latch.countDown()
        }
    }
}
