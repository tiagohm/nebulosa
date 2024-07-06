package nebulosa.common.concurrency.atomic

import nebulosa.common.Resettable
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier

class Incrementer(private val initialValue: Long = 0L) : Number(), Supplier<Long>, Resettable {

    private val incrementer = AtomicLong(initialValue)

    fun increment() = incrementer.incrementAndGet()

    override fun reset() = reset(initialValue)

    fun reset(value: Long) = incrementer.set(value)

    override fun get() = incrementer.get()

    override fun toByte() = toLong().toByte()

    override fun toDouble() = toLong().toDouble()

    override fun toFloat() = toLong().toFloat()

    override fun toInt() = toLong().toInt()

    override fun toLong() = get()

    override fun toShort() = toLong().toShort()

    override fun toString() = "Incrementer(value=${get()})"
}
