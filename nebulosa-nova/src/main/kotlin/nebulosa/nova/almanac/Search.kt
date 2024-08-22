package nebulosa.nova.almanac

import nebulosa.constants.DAYSEC
import nebulosa.math.evenlySpacedNumbers

/**
 * Computes the n-th discrete difference along the list and
 * returns indices that are non-zero.
 */
fun IntArray.computeDiffAndReduceToIndices(): IntArray {
    var count = 0

    for (i in 0 until size - 1) {
        val diff = this[i + 1] - this[i]
        if (diff != 0) count++
    }

    val res = IntArray(count)
    count = 0

    for (i in 0 until size - 1) {
        val diff = this[i + 1] - this[i]
        if (diff != 0) res[count++] = i
    }

    return res
}

private val EMPTY_X = DoubleArray(0)
private val EMPTY_Y = IntArray(0)

data class DiscreteResult(
    private val x: DoubleArray = EMPTY_X,
    private val y: IntArray = EMPTY_Y,
) {

    init {
        require(x.size == y.size)
    }

    val size
        get() = x.size

    fun x(index: Int) = x[index]

    fun y(index: Int) = y[index]

    override fun toString() = buildString {
        append("DiscreteResult(")
        repeat(size) { append("[${x[it]} | ${y[it]}]") }
        append(")")
    }

    companion object {

        @JvmStatic val EMPTY = DiscreteResult()
    }
}

/**
 * Find the times at which a discrete function of time changes value.
 *
 * This method is used to find instantaneous events like sunrise,
 * transits, and the seasons.
 */
fun findDiscrete(
    range: DoubleArray,
    action: DiscreteFunction,
    epsilon: Double = 0.001 / DAYSEC,
): DiscreteResult {
    var x = range

    while (true) {
        val y = IntArray(x.size) { action(x[it]) }
        val indices = y.computeDiffAndReduceToIndices()

        if (indices.isEmpty()) return DiscreteResult.EMPTY

        val starts = DoubleArray(indices.size) { x[indices[it]] }
        val ends = DoubleArray(indices.size) { x[indices[it] + 1] }

        if (ends[0] - starts[0] > epsilon) {
            val size = indices.size * 8
            if (size != x.size) x = DoubleArray(size)

            for (i in indices.indices) {
                evenlySpacedNumbers(starts[i], ends[i], 8).copyInto(x, i * 8)
            }
        } else {
            return DiscreteResult(ends, IntArray(indices.size) { y[indices[it] + 1] })
        }
    }
}
