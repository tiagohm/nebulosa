package nebulosa.erfa

import nebulosa.math.Angle
import nebulosa.math.Matrix3D

data class PrecessionNutationAnglesAndMatrices(
    @JvmField val dpsi: Angle, @JvmField val deps: Angle, // nutation
    @JvmField val meanObliquity: Angle, // epsa
    @JvmField val frameBias: Matrix3D, // rb
    @JvmField val precession: Matrix3D, // rp
    @JvmField val biasPrecession: Matrix3D, // rbp
    @JvmField val nutation: Matrix3D, // rn
    @JvmField val gcrsToTrue: Matrix3D, // rbpn
)
