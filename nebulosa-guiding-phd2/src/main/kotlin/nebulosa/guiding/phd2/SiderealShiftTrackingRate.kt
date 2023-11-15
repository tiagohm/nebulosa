package nebulosa.guiding.phd2

import nebulosa.math.Angle
import nebulosa.math.PairOfAngle
import java.time.Duration

data class SiderealShiftTrackingRate(
    val raPerHour: Angle, val decPerHour: Angle,
    val enabled: Boolean = true,
) {

    constructor(start: PairOfAngle, end: PairOfAngle, between: Duration) : this(
        (end.first - start.first) / between.toMillis() / 3.6,
        (end.second - start.second) / between.toMillis() / 3.6,
    )

    companion object {

        @JvmStatic val DISABLED = SiderealShiftTrackingRate(0.0, 0.0, false)
    }
}
