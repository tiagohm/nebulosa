package nebulosa.coordinates

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Vector3D

class CartesianCoordinate(
    val x: Distance,
    val y: Distance,
    val z: Distance,
) : Coordinate, Vector3D(x.value, y.value, z.value) {

    val spherical by lazy { SphericalCoordinate.of(x, y, z) }

    companion object {

        @JvmStatic
        fun of(
            theta: Angle,
            phi: Angle,
            r: Distance,
        ): CartesianCoordinate {
            val rxy = r.value * theta.cos
            val x = (rxy * phi.cos).au
            val y = (rxy * phi.sin).au
            val z = (r.value * theta.sin).au
            return CartesianCoordinate(x, y, z)
        }
    }
}
