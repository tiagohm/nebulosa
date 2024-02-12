package nebulosa.common.concurrency.atomic

import java.util.concurrent.atomic.AtomicLong

class Incrementer(initialValue: Long = 0L) : Number() {

    private val incrementer = AtomicLong(initialValue)

    fun increment() = incrementer.incrementAndGet()

    fun reset() = incrementer.set(0)

    fun get() = incrementer.get()

    override fun toByte() = toLong().toByte()

    override fun toDouble() = toLong().toDouble()

    override fun toFloat() = toLong().toFloat()

    override fun toInt() = toLong().toInt()

    override fun toLong() = get()

    override fun toShort() = toLong().toShort()
}
