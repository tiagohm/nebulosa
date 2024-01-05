package nebulosa.indi.device.mount

import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.math.toHours
import kotlin.math.abs

enum class PierSide {
    NEITHER,
    WEST,
    EAST;

    companion object {

        @JvmStatic
        fun expectedPierSide(rightAscension: Angle, declination: Angle, lst: Angle): PierSide {
            if (abs(declination.toDegrees) == 90.0) return NEITHER
            val hoursToLST = (rightAscension.toHours - lst.toHours + 24.0) % 24.0
            return if (hoursToLST < 12.0) WEST else EAST
        }
    }
}
