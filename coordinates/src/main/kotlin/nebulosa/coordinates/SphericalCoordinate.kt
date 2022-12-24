package nebulosa.coordinates

import nebulosa.erfa.eraP2s
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au

class SphericalCoordinate(
    val theta: Angle,
    val phi: Angle,
    val distance: Distance,
) : Coordinate {

    override fun component1() = theta.value

    override fun component2() = phi.value

    override fun component3() = distance.value

    inline val longitude get() = theta

    inline val latitude get() = phi

    val cartesian by lazy { CartesianCoordinate.of(theta, phi, distance) }

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
        fun of(x: Distance, y: Distance, z: Distance): SphericalCoordinate {
            val (theta, phi, r) = eraP2s(x.value, y.value, z.value)
            return SphericalCoordinate(theta, phi, r.au)
        }
    }
}
