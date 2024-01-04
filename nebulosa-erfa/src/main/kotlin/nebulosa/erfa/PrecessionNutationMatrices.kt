package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Matrix3D

data class PrecessionNutationMatrices(
    @JvmField val meanObliquity: Angle,
    @JvmField val frameBias: Matrix3D,
    @JvmField val precession: Matrix3D,
    @JvmField val biasPrecession: Matrix3D,
    @JvmField val nutation: Matrix3D,
    @JvmField val gcrsToTrue: Matrix3D,
)
