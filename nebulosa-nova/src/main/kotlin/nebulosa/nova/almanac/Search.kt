package nebulosa.nova.almanac

import nebulosa.constants.DAYSEC

/**
 * Returns evenly spaced numbers over a specified interval.
 */
private fun evenlySpacedNumbers(
    a: Double, b: Double, n: Int,
    endpoint: Boolean = true,
): DoubleArray {
    val div = if (endpoint) n - 1 else n
    val res = DoubleArray(n)
    val step = (b - a) / div
    var c = a

    repeat(res.size) {
        res[it] = c
        c += step
    }

    if (endpoint) res[res.size - 1] = b

    return res
}

/**
 * Computes the n-th discrete difference along the list and
 * returns indices that are non-zero.
 */
private fun IntArray.computeDiffAndReduceToIndices(): IntArray {
    val res = ArrayList<Int>(size - 1)

    for (i in 0 until size - 1) {
        val diff = this[i + 1] - this[i]
        if (diff != 0) res.add(i)
    }

    return IntArray(res.size) { res[it] }
}

/**
 * Find the times at which a discrete function of time changes value.
 *
 * This method is used to find instantaneous events like sunrise,
 * transits, and the seasons.
 */
fun findDiscrete(
    start: Double, end: Double,
    action: DiscreteFunction,
    epsilon: Double = 0.001 / DAYSEC, // 1 ms.
): Pair<DoubleArray, IntArray> {
    val num = 8

    require(start < end) { "your start time $start is later than your end time $end" }

    var times = evenlySpacedNumbers(start, end, ((end - start) / action.stepSize).toInt() + 2)

    while (true) {
        val y = IntArray(times.size) { action.compute(times[it]) }
        val indices = y.computeDiffAndReduceToIndices()

        if (indices.isEmpty()) return DoubleArray(0) to IntArray(0)

        val starts = DoubleArray(indices.size) { times[indices[it]] }
        val ends = DoubleArray(indices.size) { times[indices[it] + 1] }

        if (ends[0] - starts[0] > epsilon) {
            val size = indices.size * num
            if (size != times.size) times = DoubleArray(size)
            for (i in indices.indices) evenlySpacedNumbers(starts[i], ends[i], num).copyInto(times, i * num)
        } else {
            return ends to IntArray(indices.size) { y[indices[it] + 1] }
        }
    }
}
