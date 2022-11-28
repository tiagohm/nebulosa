package nebulosa.nova.frame

import nebulosa.math.Matrix3D
import nebulosa.time.InstantOfTime

/**
 * The International Coordinate Reference System (ICRS).
 *
 * The ICRS is a permanent reference frame which has replaced J2000,
 * with which its axes agree to within 0.02 arcseconds (closer than the
 * precision of J2000 itself).  The ICRS also supersedes older
 * equinox-based systems like B1900 and B1950.
 */
object ICRS : Frame {

    private val IDENTITY = Matrix3D(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)

    override fun rotationAt(time: InstantOfTime) = IDENTITY
}
