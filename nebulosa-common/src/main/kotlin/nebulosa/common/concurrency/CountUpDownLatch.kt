package nebulosa.common.concurrency

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min

class CountUpDownLatch : AtomicBoolean(true) {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val counter = AtomicInteger(0)

    fun countUp(n: Int = 1) = lock.withLock {
        require(n >= 1) { "n < 1: $n" }
        val value = counter.addAndGet(n)
        set(false)
        condition.signalAll()
        value
    }

    fun countDown(n: Int = 1) = lock.withLock {
        require(n >= 1) { "n < 1: $n" }
        val maxToSubtract = min(n, counter.get())
        val value = counter.addAndGet(-maxToSubtract)
        set(value == 0)
        condition.signalAll()
        value
    }

    fun reset(n: Int = 0) = lock.withLock {
        require(n >= 0) { "n < 0: $n" }
        counter.set(n)
        set(n == 0)
        condition.signalAll()
    }

    fun await(n: Int = 0) = lock.withLock {
        require(n >= 0) { "n < 0: $n" }

        while (counter.get() > n) {
            condition.await()
        }
    }

    fun await(time: Long, unit: TimeUnit, n: Int = 0) = lock.withLock {
        require(n >= 0) { "n < 0: $n" }
        require(time > 0L) { "time <= 0: $time" }

        var remainingTime = unit.toNanos(time)

        while (counter.get() > n) {
            val startTime = System.nanoTime()
            condition.await(remainingTime, TimeUnit.NANOSECONDS)
            val delta = System.nanoTime() - startTime
            remainingTime -= delta
        }
    }
}
