package nebulosa.nova.position

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.Body
import nebulosa.nova.frame.ITRS
import nebulosa.time.InstantOfTime

/**
 * An |xyz| position in the Earth-centered Earth-fixed (ECEF) ITRS frame.
 */
@Suppress("NOTHING_TO_INLINE")
abstract class ITRSPosition(val itrs: Vector3D) : Body, Number() {

    val velocity by lazy { Vector3D(-itrs[1] * ANGULAR_VELOCITY_PER_DAY, itrs[0] * ANGULAR_VELOCITY_PER_DAY, 0.0) }

    inline val x
        get() = itrs.x.au

    inline val y
        get() = itrs.y.au

    inline val z
        get() = itrs.z.au

    inline operator fun component1() = x

    inline operator fun component2() = y

    inline operator fun component3() = z

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
