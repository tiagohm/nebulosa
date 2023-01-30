package nebulosa.desktop.logic.concurrency

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CountUpDownLatch : AtomicBoolean(true) {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val counter = AtomicInteger(0)

    fun countUp() = lock.withLock {
        val value = counter.getAndIncrement()
        set(false)
        condition.signalAll()
        value
    }

    fun countDown() = lock.withLock {
        val value = counter.decrementAndGet()
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
}
