package nebulosa.time

/**
 * Stardate: date [units] from 2318-07-05 12:00:00 UTC.
 *
 * For example, stardate 41153.7 is 00:52 on April 30, 2363.
 *
 * Must be used with [TAI] date.
 *
 * @see <a href="http://trekguide.com/Stardates.htm#TNG">Calculations and reference points</a>
 */
data class TimeStarDate(val units: Double) : TimeFromEpoch(units, 1 / 0.397766856, 2567877.0, -0.041666666666666685)
