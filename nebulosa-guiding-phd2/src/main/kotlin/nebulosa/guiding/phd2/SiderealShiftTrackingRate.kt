package nebulosa.guiding.phd2

import nebulosa.math.Angle
import nebulosa.math.PairOfAngle
import kotlin.time.Duration

data class SiderealShiftTrackingRate(
    val raPerHour: Angle, val decPerHour: Angle,
    val enabled: Boolean = true,
) {

    constructor(start: PairOfAngle, end: PairOfAngle, between: Duration) : this(
        (end.first - start.first) / between.inWholeSeconds / 3600.0,
        (end.second - start.second) / between.inWholeSeconds / 3600.0,
    )

    companion object {

        @JvmStatic val DISABLED = SiderealShiftTrackingRate(0.0, 0.0, false)
    }
}
