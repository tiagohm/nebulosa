package nebulosa.nova.position

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.coordinates.Coordinate
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.Body
import nebulosa.nova.frame.ITRS
import nebulosa.time.InstantOfTime

/**
 * An |xyz| position in the Earth-centered Earth-fixed (ECEF) ITRS frame.
 */
interface ITRSPosition : Body, Coordinate {

    val itrs: Vector3D

    val x get() = itrs.a1

    val y get() = itrs.a2

    val z get() = itrs.a3

    override fun component1() = x

    override fun component2() = y

    override fun component3() = z

    /**
     * Gets the velocity in AU/day.
     */
    val velocity get() = Vector3D(-itrs[1], itrs[0], 0.0).also { it * (ANGULAR_VELOCITY * DAYSEC) }

    /**
     * Computes GCRS position and velocity at the [time].
     */
    override fun compute(time: InstantOfTime): Pair<Vector3D, Vector3D> {
        val rt = ITRS.rotationAt(time).transpose()
        val r = rt * itrs
        val v = rt * velocity
        return r to v
    }
}
