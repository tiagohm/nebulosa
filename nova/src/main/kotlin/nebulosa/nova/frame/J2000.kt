package nebulosa.nova.frame

import nebulosa.math.Matrix3D

/**
 * Earth mean equator, dynamical equinox of J2000.
 */
object J2000 : InertialFrame(Matrix3D(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0))
