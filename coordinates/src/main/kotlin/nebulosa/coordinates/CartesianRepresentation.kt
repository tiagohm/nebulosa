package nebulosa.coordinates

import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Vector3D

class CartesianRepresentation(
    val x: Distance,
    val y: Distance,
    val z: Distance,
) : Representation, Vector3D(x.value, y.value, z.value) {

    constructor(vector: Vector3D) : this(vector.a1.au, vector.a2.au, vector.a3.au)

    fun spherical() = SphericalRepresentation.of(x, y, z)

    companion object {

        @JvmStatic
        fun of(
            theta: Angle,
            phi: Angle,
            r: Distance,
        ): CartesianRepresentation {
            val rxy = r.value * theta.cos
            val x = (rxy * phi.cos).au
            val y = (rxy * phi.sin).au
            val z = (r.value * theta.sin).au
            return CartesianRepresentation(x, y, z)
        }
    }
}
