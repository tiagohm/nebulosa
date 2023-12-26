package nebulosa.common.concurrency

import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import kotlin.math.max

class CountUpDownLatch(initialCount: Int = 0) : AtomicBoolean(initialCount == 0), CancellationListener {

    private val sync = Sync(this)

    init {
        require(initialCount >= 0) { "initialCount < 0: $initialCount" }
        sync.count = initialCount
    }

    val count
        get() = sync.count

    @Synchronized
    fun countUp(n: Int = 1): Int {
        if (n >= 1) {
            sync.count += n
            set(false)
        }

        return count
    }

    @Synchronized
    fun countDown(n: Int = 1): Int {
        if (n >= 1) sync.releaseShared(n)
        return count
    }

    @Synchronized
    fun reset(n: Int = 0): Int {
        return countDown(count - n)
    }

    fun await(n: Int = 0) {
        if (n >= 0) sync.acquireSharedInterruptibly(n)
    }

    fun await(timeout: Long, unit: TimeUnit, n: Int = 0): Boolean {
        return n >= 0 && sync.tryAcquireSharedNanos(n, unit.toNanos(timeout))
    }

    fun await(timeout: Duration, n: Int = 0): Boolean {
        return n >= 0 && sync.tryAcquireSharedNanos(n, timeout.toNanos())
    }

    override fun accept(source: CancellationSource) {
        if (source !== CancellationSource.None) {
            reset()
        }
    }

    private class Sync(private val latch: AtomicBoolean) : AbstractQueuedSynchronizer() {

        var count
            get() = state
            set(value) {
                state = value
            }

        override fun tryAcquireShared(acquires: Int): Int {
            return if (state == acquires) 1 else -1
        }

        override fun tryReleaseShared(releases: Int): Boolean {
            while (true) {
                with(state) {
                    if (this == 0) return false

                    val next = max(0, this - releases)

                    if (compareAndSetState(this, next)) {
                        latch.set(next <= state)
                        return latch.get()
                    }
                }
            }
        }
    }
}
