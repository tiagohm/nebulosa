package nebulosa.alignment.polar.point.three

import nebulosa.constants.PIOVERTWO
import nebulosa.erfa.eraAtoc13
import nebulosa.math.Angle
import nebulosa.math.ONE_ATM
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime
import nebulosa.time.UTC

data class Topocentric(
    @JvmField val azimuth: Angle, @JvmField val altitude: Angle,
    @JvmField val longitude: Angle, @JvmField val latitude: Angle,
) {

    fun transform(time: InstantOfTime = UTC.now()): DoubleArray {
        val dut1 = IERS.delta(time)
        val (xp, yp) = IERS.pmAngles(time)
        val zd = PIOVERTWO - altitude // zenith distance

        return eraAtoc13('A', azimuth, zd, time.utc.whole, time.utc.fraction, dut1, longitude, latitude, 0.0, xp, yp, ONE_ATM, 15.0, 0.5, 0.55)
    }
}
