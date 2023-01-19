package nebulosa.desktop.logic.concurrency

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CountUpDownLatch {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val counter = AtomicInteger(0)

    fun countUp() = lock.withLock {
        val value = counter.getAndIncrement()
        condition.signalAll()
        value
    }

    fun countDown() = lock.withLock {
        val value = counter.decrementAndGet()
        condition.signalAll()
        value
    }

    fun reset(n: Int = 0) = lock.withLock {
        counter.set(n)
        condition.signalAll()
    }

    fun await(n: Int = 0) = lock.withLock {
        while (counter.get() > n) {
            condition.await()
        }
    }
}
