package nebulosa.curve.fitting

import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import kotlin.math.pow

object RSquared {

    fun calculate(points: Collection<CurvePoint>, function: UnivariateFunction): Double {
        val descriptiveStatistics = DescriptiveStatistics(points.size)
        val predictedValues = DoubleArray(points.size)
        var residualSumOfSquares = 0.0

        for ((i, point) in points.withIndex()) {
            val actualValue = point.y
            val predictedValue = function.value(point.x)
            predictedValues[i] = predictedValue

            val t = (predictedValue - actualValue).pow(2.0)
            residualSumOfSquares += t
            descriptiveStatistics.addValue(actualValue)
        }

        val avgActualValues = descriptiveStatistics.mean
        var totalSumOfSquares = 0.0

        repeat(points.size) {
            totalSumOfSquares += (predictedValues[it] - avgActualValues).pow(2.0)
        }

        return 1.0 - (residualSumOfSquares / totalSumOfSquares)
    }
}
