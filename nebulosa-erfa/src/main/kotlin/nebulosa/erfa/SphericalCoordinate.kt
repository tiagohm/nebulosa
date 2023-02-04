package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Distance

@Suppress("NOTHING_TO_INLINE")
data class SphericalCoordinate(
    val theta: Angle,
    val phi: Angle,
    val distance: Distance,
) {

    val cartesian by lazy { CartesianCoordinate.of(theta, phi, distance) }

    inline val longitude
        get() = theta

    inline val latitude
        get() = phi

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SphericalCoordinate) return false

        if (theta != other.theta) return false
        if (phi != other.phi) return false
        if (distance != other.distance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = theta.hashCode()
        result = 31 * result + phi.hashCode()
        result = 31 * result + distance.hashCode()
        return result
    }

    override fun toString(): String {
        return "SphericalCoordinate(theta=$theta, phi=$phi, distance=$distance)"
    }

    companion object {

        @JvmStatic
        fun of(x: Distance, y: Distance, z: Distance) = eraP2s(x, y, z)
    }
}
