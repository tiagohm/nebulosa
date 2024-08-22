package nebulosa.horizons

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.toDegrees
import nebulosa.math.toKilometers

sealed interface ObservingSite {

    val code: Int

    val center: String

    val coord: String

    sealed interface Topocentric : ObservingSite {

        val longitude: Angle

        val latitude: Angle

        val elevation: Distance
    }

    data class Geocentric(override val code: Int) : ObservingSite {

        override val center = "geo@$code"
        override val coord = "0,0,0"

        companion object {

            @JvmStatic val EARTH = Geocentric(399)
        }
    }

    data class Planetographic(
        override val code: Int,
        override val longitude: Angle, override val latitude: Angle, override val elevation: Distance = 0.0,
    ) : Topocentric {

        override val center = "coord@$code"
        override val coord = "${longitude.toDegrees},${latitude.toDegrees},${elevation.toKilometers}"
    }

    data class Geographic(override val longitude: Angle, override val latitude: Angle, override val elevation: Distance = 0.0) : Topocentric {

        override val code = 399
        override val center = "coord"
        override val coord = "${longitude.toDegrees},${latitude.toDegrees},${elevation.toKilometers}"
    }
}
