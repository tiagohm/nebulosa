package nebulosa.common.concurrency

import nebulosa.log.loggerFor
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min
import kotlin.time.Duration

abstract class ThreadedJob<T> : LinkedList<T>(), Runnable {

    private val mRunning = AtomicBoolean()
    private val pauser = CountUpDownLatch()

    val paused
        get() = !pauser.get()

    val running
        get() = mRunning.get()

    protected abstract fun execute()

    protected open fun onStart() = Unit

    protected open fun onStop() = Unit

    final override fun run() {
        if (mRunning.compareAndSet(false, true)) {
            try {
                onStart()

                while (mRunning.get()) {
                    pauser.await()

                    if (mRunning.get()) {
                        execute()
                    }
                }
            } catch (e: Throwable) {
                LOG.error("job error", e)
            } finally {
                mRunning.set(false)
                onStop()
            }
        }
    }

    fun pause() {
        if (!paused) {
            pauser.countUp()
        }
    }

    fun unpause() {
        if (paused) {
            pauser.countDown()
        }
    }

    fun stop() {
        mRunning.set(false)
        pauser.reset()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ThreadedJob<*>>()

        private const val DELAY_INTERVAL = 100L

        @JvmStatic
        fun sleep(delay: Duration, abort: AtomicBoolean) {
            var remainingTime = delay.inWholeMilliseconds

            while (!abort.get() && remainingTime > 0L) {
                Thread.sleep(min(remainingTime, DELAY_INTERVAL))
                remainingTime -= DELAY_INTERVAL
            }
        }
    }
}
