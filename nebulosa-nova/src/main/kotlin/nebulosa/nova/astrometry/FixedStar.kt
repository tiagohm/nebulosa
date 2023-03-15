package nebulosa.nova.astrometry

import nebulosa.constants.*
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Angle
import nebulosa.math.Vector3D
import nebulosa.math.Velocity
import nebulosa.nova.position.ICRF
import nebulosa.time.InstantOfTime
import nebulosa.time.TimeJD

/**
 * The position in the sky of a star or other "fixed" object.
 *
 * Each Star object specifies the position of a distant object. You
 * should provide as a right ascension and declination relative to the
 * ICRS (the recent improvement upon J2000).
 *
 * For objects whose proper motion across the sky has been detected,
 * you can supply velocities in milliarcseconds (mas) per year, and
 * even a [parallax] and [radial] velocity if those are known.
 */
data class FixedStar(
    val ra: Angle,
    val dec: Angle,
    val pmRA: Angle = Angle.ZERO,
    val pmDEC: Angle = Angle.ZERO,
    val parallax: Angle = Angle.ZERO,
    val radial: Velocity = Velocity.ZERO,
    val epoch: InstantOfTime = TimeJD.J2000,
) : Body {

    private val pv by lazy { computePositionAndVelocity(ra, dec, pmRA, pmDEC, parallax, radial) }

    override val center = 0

    override val target = Int.MIN_VALUE

    override fun observedAt(observer: ICRF): PositionAndVelocity {
        // Form unit vector 'u1' in direction of star.
        val u1 = pv.position.normalized
        // Light-time returned is the projection of vector "pos_obs" onto the
        // unit vector "u1", divided by the speed of light.
        val lightTime = u1.dot(observer.position) / SPEED_OF_LIGHT_AU_DAY
        val position = pv.position + pv.velocity *
                (observer.time.tdb.whole - epoch.tt.whole + lightTime + observer.time.tdb.fraction - epoch.tt.fraction) -
                observer.position
        return PositionAndVelocity(position, observer.velocity - pv.velocity)
    }

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val position = pv.position + pv.velocity * (time.tdb.whole - epoch.tt.whole + time.tdb.fraction - epoch.tt.fraction)
        return PositionAndVelocity(position, pv.velocity)
    }

    companion object {

        @JvmStatic
        private fun computePositionAndVelocity(
            ra: Angle, dec: Angle,
            pmRA: Angle = Angle.ZERO, pmDEC: Angle = Angle.ZERO,
            parallax: Angle = Angle.ZERO, radial: Velocity = Velocity.ZERO,
        ): PositionAndVelocity {
            // Computing the star's position as an ICRF position and velocity.
            val dist = 1.0 / parallax.sin
            val cra = ra.cos
            val sra = ra.sin
            val cdc = dec.cos
            val sdc = dec.sin

            val position = Vector3D(
                dist * cdc * cra,
                dist * cdc * sra,
                dist * sdc,
            )

            // Compute Doppler factor, which accounts for change in light travel time to star.
            val k = 1.0 / (1.0 - radial.ms / SPEED_OF_LIGHT)

            // Convert proper motion and radial velocity to orthogonal
            // components of motion with units of au/day.

            val pmr = pmRA.value / (parallax.value * DAYSPERJY) * k
            val pmd = pmDEC.value / (parallax.value * DAYSPERJY) * k
            val rvl = radial.kms * DAYSEC / AU_KM * k

            val velocity = Vector3D(
                -pmr * sra - pmd * sdc * cra + rvl * cdc * cra,
                pmr * cra - pmd * sdc * sra + rvl * cdc * sra,
                pmd * cdc + rvl * sdc,
            )

            return PositionAndVelocity(position, velocity)
        }
    }
}
