package nebulosa.curve.fitting

import nebulosa.math.squared
import org.apache.commons.math3.analysis.UnivariateFunction
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

data object RSquared {

    @JvmStatic
    fun calculate(points: Collection<CurvePoint>, function: UnivariateFunction): Double {
        val descriptiveStatistics = DescriptiveStatistics(points.size)
        val predictedValues = DoubleArray(points.size)
        var residualSumOfSquares = 0.0

        points.forEachIndexed { i, point ->
            val actualValue = point.y
            val predictedValue = function.value(point.x)
            predictedValues[i] = predictedValue

            val t = (predictedValue - actualValue).squared
            residualSumOfSquares += t
            descriptiveStatistics.addValue(actualValue)
        }

        val avgActualValues = descriptiveStatistics.mean
        var totalSumOfSquares = 0.0

        repeat(points.size) {
            totalSumOfSquares += (predictedValues[it] - avgActualValues).squared
        }

        return 1.0 - (residualSumOfSquares / totalSumOfSquares)
    }
}
