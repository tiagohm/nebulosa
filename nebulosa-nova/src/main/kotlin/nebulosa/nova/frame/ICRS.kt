package nebulosa.nova.frame

import nebulosa.math.Matrix3D

/**
 * The International Celestial Reference System (ICRS).
 *
 * The ICRS is a permanent reference frame which has replaced J2000,
 * with which its axes agree to within 0.02 arcseconds (closer than the
 * precision of J2000 itself).  The ICRS also supersedes older
 * equinox-based systems like B1900 and B1950.
 */
data object ICRS : InertialFrame(Matrix3D(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0))
