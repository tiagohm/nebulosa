package nebulosa.nova.frame

import nebulosa.math.Matrix3D
import nebulosa.time.InstantOfTime

abstract class InertialFrame(val matrix: Matrix3D) : Frame {

    constructor(
        a11: Double = 0.0, a12: Double = 0.0, a13: Double = 0.0,
        a21: Double = 0.0, a22: Double = 0.0, a23: Double = 0.0,
        a31: Double = 0.0, a32: Double = 0.0, a33: Double = 0.0,
    ) : this(Matrix3D(a11, a12, a13, a21, a22, a23, a31, a32, a33))

    final override fun rotationAt(time: InstantOfTime) = matrix

    /**
     * Gets the transposed frame.
     */
    open val transposed: InertialFrame by lazy { Transposed(this) }

    protected data class Transposed(val frame: InertialFrame) : InertialFrame(frame.matrix.transpose()) {

        override val transposed get() = frame
    }
}
