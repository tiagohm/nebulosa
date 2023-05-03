package nebulosa.guiding.internal

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal class DescriptiveStats {

    var count = 0
        private set

    var mean = 0.0
        private set

    var minimum = 0.0
        private set

    var maximum = 0.0
        private set

    var maxDelta = 0.0
        private set

    private var lastValue = 0.0
    private var runningS = 0.0

    val variance
        get() = if (count > 1) runningS else 0.0

    val sigma
        get() = if (count > 1) sqrt(runningS / (count - 1)) else 0.0

    val populationSigma
        get() = if (count > 0) sqrt(runningS / count) else 0.0

    val sum
        get() = mean * count

    fun add(value: Double) {
        count++

        if (count == 1) {
            mean = value
            minimum = value
            maximum = value
            maxDelta = 0.0
        } else {
            val newMean = mean + (value - mean) / count
            val newS = runningS + (value - mean) * (value - newMean)
            mean = newMean
            runningS = newS
            minimum = min(minimum, value)
            maximum = max(maximum, value)
            val newDelta = abs(value - lastValue)
            maxDelta = max(maxDelta, newDelta)
        }

        lastValue = value
    }

    fun clear() {
        count = 0
        runningS = 0.0
        mean = 0.0
        lastValue = 0.0
        minimum = 0.0
        maximum = 0.0
        maxDelta = 0.0
    }
}
