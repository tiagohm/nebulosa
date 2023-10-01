package nebulosa.erfa

import nebulosa.math.*
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.hypot

data class CartesianCoordinate(
    val x: Distance = 0.0,
    val y: Distance = 0.0,
    val z: Distance = 0.0,
) {

    val spherical by lazy { SphericalCoordinate.of(x, y, z) }

    val vector by lazy { Vector3D(x, y, z) }

    fun angularDistance(coordinate: CartesianCoordinate): Angle {
        val dot = x * coordinate.x + y * coordinate.y + z * coordinate.z
        val norm0 = hypot(x, y)
        val norm1 = hypot(coordinate.x, coordinate.y)
        val v = dot / (norm0 * norm1)
        return if (abs(v) > 1.0) if (v < 0.0) SEMICIRCLE else 0.0
        else acos(v).rad
    }

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

        @JvmStatic val ZERO = CartesianCoordinate()

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
