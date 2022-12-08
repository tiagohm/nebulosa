package nebulosa.coordinates

import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Distance.Companion.km
import nebulosa.math.Vector3D
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

@Suppress("NOTHING_TO_INLINE")
class SphericalRepresentation(
    val theta: Angle,
    val phi: Angle,
    val distance: Distance,
) : Representation, Vector3D(theta.value, phi.value, distance.value) {

    constructor(vector: Vector3D) : this(vector.a1.rad, vector.a2.rad, vector.a3.au)

    inline val longitude get() = theta

    inline val latitude get() = phi

    inline fun cartesian() = CartesianRepresentation.of(theta, phi, distance)

    companion object {

        @JvmStatic
        fun of(
            x: Distance,
            y: Distance,
            z: Distance,
        ): SphericalRepresentation {
            val r = sqrt((x.value * x.value) + (y.value * y.value) + (z.value * z.value))
            val theta = atan2(y.value, x.value).rad
            val phi = asin(z.value / r).rad
            return SphericalRepresentation(theta, phi, r.au)
        }

        @JvmStatic
        fun of(vector: Vector3D) = of(vector[0].km, vector[1].km, vector[2].km)
    }
}
