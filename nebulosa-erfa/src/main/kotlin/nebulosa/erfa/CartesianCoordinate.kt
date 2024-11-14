package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Vector3D
import nebulosa.math.cos
import nebulosa.math.sin

class CartesianCoordinate : Vector3D {

    constructor(x: Distance = 0.0, y: Distance = 0.0, z: Distance = 0.0) : super(x, y, z)

    constructor(vector: DoubleArray) : super(vector)

    val spherical by lazy { SphericalCoordinate.of(x, y, z) }

    companion object {

        @JvmStatic val ZERO = CartesianCoordinate()

        /**
         * Given [theta] as longitude, [phi] as latitude and
         * [r] as radial distance, converts spherical polar coordinates
         * to [CartesianCoordinate].
         */
        @JvmStatic
        fun of(theta: Angle, phi: Angle, r: Distance): CartesianCoordinate {
            val cp = phi.cos
            val x = r * (theta.cos * cp)
            val y = r * (theta.sin * cp)
            val z = r * phi.sin
            return CartesianCoordinate(x, y, z)
        }
    }
}
