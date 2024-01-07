package nebulosa.time

import nebulosa.constants.J2000
import kotlin.math.sin

/**
 * TDB - TT by Fairhead & Bretagnon, 1990.
 */
object TDBMinusTTByFairheadAndBretagnon1990 : TimeDelta {

    /**
     * Computes TDB - TT in seconds at [time].
     */
    override fun delta(time: InstantOfTime): Double {
        // Given that the two time scales never diverge by more than 2ms, TT
        // can also be given as the argument to perform the conversion in the
        // other direction.
        val tdb = time.tdb

        val t = (tdb.whole - J2000 + tdb.fraction) / 36525.0

        // USNO Circular 179, eq. 2.6.
        return (0.001657 * sin(628.3076 * t + 6.2401)
                + 0.000022 * sin(575.3385 * t + 4.2970)
                + 0.000014 * sin(1256.6152 * t + 6.1969)
                + 0.000005 * sin(606.9777 * t + 4.0212)
                + 0.000005 * sin(52.9691 * t + 0.4444)
                + 0.000002 * sin(21.3299 * t + 5.5431)
                + 0.000010 * t * sin(628.3076 * t + 4.2490))
    }
}
