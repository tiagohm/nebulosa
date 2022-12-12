package nebulosa.coordinates

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Vector3D
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

class SphericalCoordinate(
    val theta: Angle,
    val phi: Angle,
    val distance: Distance,
) : Coordinate, Vector3D(theta.value, phi.value, distance.value) {

    inline val longitude get() = theta

    inline val latitude get() = phi

    val cartesian by lazy { CartesianCoordinate.of(theta, phi, distance) }

    companion object {

        @JvmStatic
        fun of(
            x: Distance,
            y: Distance,
            z: Distance,
        ): SphericalCoordinate {
            val r = sqrt((x.value * x.value) + (y.value * y.value) + (z.value * z.value))
            val theta = atan2(y.value, x.value).rad
            val phi = asin(z.value / r).rad
            return SphericalCoordinate(theta, phi, r.au)
        }
    }
}
