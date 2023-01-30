package nebulosa.guiding

import nebulosa.math.Angle
import java.util.concurrent.TimeUnit

data class SiderealShiftTrackingRate(
    val rightAscension: Angle, // per hour.
    val declination: Angle, // per hour.
) {

    companion object {

        private const val SIDEREAL_SEC_PER_SI_SEC = 1.00273791552838

        @JvmStatic val DISABLED = SiderealShiftTrackingRate(Angle.ZERO, Angle.ZERO)

        @JvmStatic
        fun of(
            ra: Angle, dec: Angle,
            period: Long, unit: TimeUnit,
        ): SiderealShiftTrackingRate {
            val seconds = unit.toSeconds(period) / 3600.0
            return SiderealShiftTrackingRate(ra / seconds, dec / seconds)
        }

        @JvmStatic
        fun of(
            raStart: Angle, decStart: Angle, raEnd: Angle, decEnd: Angle,
            period: Long, unit: TimeUnit,
        ): SiderealShiftTrackingRate {
            val ra = raEnd - raStart
            val dec = decEnd - decStart
            return of(ra, dec, period, unit)
        }
    }
}
