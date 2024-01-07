package nebulosa.guiding.phd2

import nebulosa.math.Angle
import java.time.Duration

data class SiderealShiftTrackingRate(
    val raPerHour: Angle, val decPerHour: Angle,
    val enabled: Boolean = true,
) {

    constructor(start: DoubleArray, end: DoubleArray, between: Duration) : this(
        (end[0] - start[0]) / between.toMillis() / 3.6,
        (end[1] - start[1]) / between.toMillis() / 3.6,
    )

    companion object {

        @JvmStatic val DISABLED = SiderealShiftTrackingRate(0.0, 0.0, false)
    }
}
