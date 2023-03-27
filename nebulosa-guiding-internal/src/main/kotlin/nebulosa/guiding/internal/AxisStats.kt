package nebulosa.guiding.internal

import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal open class AxisStats {

    data class LinearFit(
        val slope: Double = 0.0,
        val intercept: Double = 0.0,
        val sigma: Double = 0.0,
        val rSquared: Double = 0.0,
    ) {

        companion object {

            @JvmStatic val ZERO = LinearFit()
        }
    }

    protected val guidingEntries = LinkedList<StarDisplacement>()
    protected var prevMove = 0.0
    protected var prevPosition = 0.0
    protected var sumX = 0.0
    protected var sumY = 0.0
    protected var sumXY = 0.0
    protected var sumXSq = 0.0
    protected var sumYSq = 0.0
    protected var maxDeltaInx = 0

    var axisMoves = 0
        protected set

    var axisReversals = 0
        protected set

    var maxDelta = 0.0
        protected set

    val sum
        get() = sumY

    val mean
        get() = sum / count

    val variance
        get() = if (count > 1) (count * sumYSq - sumY * sumY) / (count * (count - 1)) else 0.0

    val sigma
        get() = sqrt(variance)

    val populationSigma
        get() = if (count > 1) sqrt((count * sumYSq - sumY * sumY) / (count * count)) else 0.0

    val median: Double
        get() {
            if (count == 0) return 0.0
            if (count == 1) return guidingEntries[0].starPos
            val sortedEntries = guidingEntries.sortedBy { it.starPos }
            val ctr = sortedEntries.size / 2
            return if (sortedEntries.size % 2 == 1) sortedEntries[ctr].starPos
            else (sortedEntries[ctr].starPos + sortedEntries[ctr - 1].starPos) / 2.0
        }

    var maxDisplacement = Double.MAX_VALUE
        protected set

    var minDisplacement = Double.MIN_VALUE
        protected set

    val count
        get() = guidingEntries.size

    val lastEntry
        get() = if (count > 0) guidingEntries.last!! else StarDisplacement.ZERO

    /**
     * Adds a guiding info element of relative time,
     * guide star position, guide pulse amount.
     */
    open fun add(deltaTime: Double, starPos: Double, guideAmount: Double) {
        minDisplacement = min(starPos, minDisplacement)
        maxDisplacement = max(starPos, maxDisplacement)

        sumX += deltaTime
        sumXY += deltaTime * starPos
        sumXSq += deltaTime * deltaTime
        sumYSq += starPos * starPos
        sumY += starPos

        var guided = false
        var reversal = false

        if (guideAmount != 0.0) {
            guided = true

            axisMoves++

            if (guideAmount * prevMove < 0.0) {
                axisReversals++
                reversal = true
            }

            prevMove = guideAmount
        }

        if (count > 1) {
            val newDelta = abs(starPos - prevPosition)

            if (newDelta >= maxDelta) {
                maxDelta = newDelta
                // Where the entry is going to go - furthest down in list among equals.
                maxDeltaInx = count
            }
        }

        val starInfo = StarDisplacement(deltaTime, starPos, guided, reversal)
        guidingEntries.add(starInfo)

        prevPosition = starPos
    }

    /**
     * Returns a particular element from the current dataset.
     */
    operator fun get(index: Int): StarDisplacement {
        return if (index in guidingEntries.indices) guidingEntries[index]
        else StarDisplacement.ZERO
    }

    fun reset() {
        axisMoves = 0
        axisReversals = 0
        sumY = 0.0
        sumYSq = 0.0
        sumX = 0.0
        sumXY = 0.0
        sumXSq = 0.0
        prevPosition = 0.0
        prevMove = 0.0
        minDisplacement = Double.MAX_VALUE
        maxDisplacement = Double.MIN_VALUE
        maxDelta = 0.0
    }

    fun clear() {
        guidingEntries.clear()
        reset()
    }

    fun linearFit(
        computeSigma: Boolean = false,
        computeRSquared: Boolean = false,
    ): LinearFit {
        if (count <= 1) return LinearFit.ZERO

        val slope = (count * sumXY - sumX * sumY) / (count * sumXSq - sumX * sumX)
        // Possible future use, slope value if intercept is constrained to be zero.
        // val constrainedSlope = sumXY / sumXSq
        val intercept = (sumY - slope * sumX) / count

        var sigma = 0.0

        if (computeSigma) {
            var currentMean = Double.NaN
            var currentVariance = 0.0

            // Apply the linear fit to the data points and compute their resultant sigma.
            for (entry in guidingEntries) {
                val newValue = entry.starPos - (entry.deltaTime * slope + intercept)

                if (currentMean.isNaN()) {
                    currentMean = newValue
                } else {
                    val delta = newValue - currentMean
                    val newMean = currentMean + delta / count
                    currentVariance += delta * delta
                    currentMean = newMean
                }
            }

            sigma = sqrt(currentVariance / (count - 1))
        }

        var rSquared = 0.0

        if (computeRSquared) {
            // Compute R-Squared coefficient of determination.
            val syy = sumYSq - sumY * sumY / count
            val sxy = sumXY - sumX * sumY / count
            val sxx = sumXSq - sumX * sumX / count
            val sse = syy - sxy * sxy / sxx
            rSquared = (syy - sse) / syy
        }

        return LinearFit(slope, intercept, sigma, rSquared)
    }
}
