package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Vector3D

data class CartesianCoordinate(
    val x: Distance,
    val y: Distance,
    val z: Distance,
) {

    val spherical by lazy { SphericalCoordinate.of(x, y, z) }

    val vector3D by lazy { Vector3D(x.value, y.value, z.value) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartesianCoordinate) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    override fun toString(): String {
        return "CartesianCoordinate(x=$x, y=$y, z=$z)"
    }

    companion object {

        @JvmStatic val EMPTY = CartesianCoordinate(Distance.ZERO, Distance.ZERO, Distance.ZERO)

        /**
         * Given [theta] as longitude, [phi] as latitude and
         * [r] as radial distance, converts spherical polar coordinates
         * to [CartesianCoordinate].
         */
        @JvmStatic
        fun of(
            theta: Angle,
            phi: Angle,
            r: Distance,
        ): CartesianCoordinate {
            val cp = phi.cos
            val x = r * (theta.cos * cp)
            val y = r * (theta.sin * cp)
            val z = r * phi.sin
            return CartesianCoordinate(x, y, z)
        }
    }
}
