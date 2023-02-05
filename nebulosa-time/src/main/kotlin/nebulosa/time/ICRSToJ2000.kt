package nebulosa.time

import nebulosa.constants.ASEC2RAD
import nebulosa.math.Matrix3D

private const val XI = (-0.0166170) * ASEC2RAD
private const val ETA = (-0.0068192) * ASEC2RAD
private const val DA = (-0.01460) * ASEC2RAD

// Compute elements of rotation matrix.
private const val A10 = -DA
private const val A20 = XI
private const val A01 = DA
private const val A21 = ETA
private const val A02 = -XI
private const val A12 = -ETA

// Include second-order corrections to diagonal elements.
private const val A00 = 1.0 - 0.5 * (A10 * A10 + A20 * A20)
private const val A11 = 1.0 - 0.5 * (A10 * A10 + A21 * A21)
private const val A22 = 1.0 - 0.5 * (A21 * A21 + A20 * A20)

val ICRSToJ2000 = Matrix3D(A00, A01, A02, A10, A11, A12, A20, A21, A22)
