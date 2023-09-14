package nebulosa.common.concurrency

import java.util.concurrent.atomic.AtomicInteger

class Incrementer(initialValue: Int = 0) : Number() {

    private val incrementer = AtomicInteger(initialValue)

    fun increment() = incrementer.incrementAndGet()

    fun reset() = incrementer.set(0)

    fun get() = incrementer.get()

    override fun toByte() = toInt().toByte()

    override fun toDouble() = toInt().toDouble()

    override fun toFloat() = toInt().toFloat()

    override fun toInt() = get()

    override fun toLong() = toInt().toLong()

    override fun toShort() = toInt().toShort()
}
