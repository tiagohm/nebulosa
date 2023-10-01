package nebulosa.nova.position

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Vector3D
import nebulosa.math.au
import nebulosa.nova.astrometry.Body
import nebulosa.nova.frame.ITRS
import nebulosa.time.InstantOfTime

/**
 * An |xyz| position in the Earth-centered Earth-fixed (ECEF) ITRS frame.
 */
abstract class ITRSPosition(private val itrs: Vector3D) : Body, Number() {

    val velocity = Vector3D(-itrs[1] * ANGULAR_VELOCITY_PER_DAY, itrs[0] * ANGULAR_VELOCITY_PER_DAY)

    val x = itrs.x.au

    val y = itrs.y.au

    val z = itrs.z.au

    operator fun component1() = x

    operator fun component2() = y

    operator fun component3() = z

    /**
     * Computes GCRS position and velocity at the [time].
     */
    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val rt = ITRS.rotationAt(time).transposed
        val r = rt * itrs
        val v = rt * velocity
        return PositionAndVelocity(r, v)
    }

    companion object {

        private const val ANGULAR_VELOCITY_PER_DAY = ANGULAR_VELOCITY * DAYSEC
    }
}
