package nebulosa.alignment.polar.point.three

import nebulosa.constants.PIOVERTWO
import nebulosa.math.Angle

data class Topocentric(@JvmField val azimuth: Angle, @JvmField val altitude: Angle) {

    companion object {

        @JvmStatic val ZERO = Topocentric(0.0, PIOVERTWO)
    }
}
