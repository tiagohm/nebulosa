package nebulosa.time

import kotlin.math.round

/**
 * Base class for times that represent the interval from a particular
 * epoch as a floating point multiple of a unit time interval (e.g. seconds
 * or days).
 */
abstract class TimeFromEpoch(
    epoch: Double,
    unit: Double,
    epochStartWhole: Double,
    epochStartFraction: Double,
) : TimeJD(compute(epoch, unit, epochStartWhole, epochStartFraction)) {

    companion object {

        @JvmStatic
        private fun compute(
            epoch: Double,
            unit: Double,
            epochStartWhole: Double,
            epochStartFraction: Double,
        ): DoubleArray {
            val date = normalize(epoch, divisor = unit)

            date[0] += epochStartWhole
            date[1] += epochStartFraction

            val extra = round(date[1])
            date[0] += extra
            date[1] -= extra

            return date
        }
    }
}
