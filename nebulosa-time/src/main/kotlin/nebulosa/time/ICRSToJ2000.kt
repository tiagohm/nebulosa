package nebulosa.time

import nebulosa.math.Angle.Companion.arcsec
import nebulosa.math.Matrix3D

object ICRSToJ2000 : Matrix3D() {

    private val xi = (-0.0166170).arcsec.value
    private val eta = (-0.0068192).arcsec.value
    private val da = (-0.01460).arcsec.value

    init {
        // Compute elements of rotation matrix.
        this[1, 0] = -da
        this[2, 0] = xi
        this[0, 1] = da
        this[2, 1] = eta
        this[0, 2] = -xi
        this[1, 2] = -eta

        // Include second-order corrections to diagonal elements.
        this[0, 0] = 1.0 - 0.5 * (this[1, 0] * this[1, 0] + this[2, 0] * this[2, 0])
        this[1, 1] = 1.0 - 0.5 * (this[1, 0] * this[1, 0] + this[2, 1] * this[2, 1])
        this[2, 2] = 1.0 - 0.5 * (this[2, 1] * this[2, 1] + this[2, 0] * this[2, 0])
    }
}
