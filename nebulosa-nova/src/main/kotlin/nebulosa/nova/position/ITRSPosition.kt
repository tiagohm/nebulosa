package nebulosa.nova.position

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.Body
import nebulosa.nova.frame.ITRS
import nebulosa.time.InstantOfTime

/**
 * An |xyz| position in the Earth-centered Earth-fixed (ECEF) ITRS frame.
 */
interface ITRSPosition : Body {

    val itrs: Vector3D

    /**
     * Gets the velocity in AU/day.
     */
    val velocity get() = Vector3D(-itrs[1], itrs[0], 0.0).also { it * (ANGULAR_VELOCITY * DAYSEC) }

    /**
     * Computes GCRS position and velocity at the [time].
     */
    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val rt = ITRS.rotationAt(time).transposed
        val r = rt * itrs
        val v = rt * velocity
        return PositionAndVelocity(r, v)
    }
}
