package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Vector3D
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.hypot

data class CartesianCoordinate(
    val x: Distance,
    val y: Distance,
    val z: Distance,
) {

    val spherical by lazy { SphericalCoordinate.of(x, y, z) }

    val vector by lazy { Vector3D(x.value, y.value, z.value) }

    fun angularDistance(coordinate: CartesianCoordinate): Angle {
        val dot = x.value * coordinate.x.value + y.value * coordinate.y.value + z.value * coordinate.z.value
        val norm0 = hypot(x.value, y.value)
        val norm1 = hypot(coordinate.x.value, coordinate.y.value)
        val v = dot / (norm0 * norm1)
        return if (abs(v) > 1.0) if (v < 0.0) Angle.SEMICIRCLE else Angle.ZERO
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
