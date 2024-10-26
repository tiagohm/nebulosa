package nebulosa.nova.astrometry

import nebulosa.constants.*
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Angle
import nebulosa.math.Vector3D
import nebulosa.math.Velocity
import nebulosa.math.toMetersPerSecond
import nebulosa.nova.position.ICRF
import nebulosa.time.InstantOfTime
import nebulosa.time.UTC
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * The position in the sky of a star or other "fixed" object.
 *
 * Each Star object specifies the position of a distant object. You
 * should provide as a right ascension and declination relative to the
 * ICRS (the recent improvement upon J2000).
 *
 * For objects whose proper motion across the sky has been detected,
 * you can supply velocities in milliarcseconds (mas) per year, and
 * even a [parallax] and [radialVelocity] velocity if those are known.
 */
data class FixedStar(
    val ra: Angle,
    val dec: Angle,
    val pmRA: Angle = 0.0,
    val pmDEC: Angle = 0.0,
    val parallax: Angle = 0.0,
    val radialVelocity: Velocity = 0.0,
    val epoch: InstantOfTime = UTC.J2000,
) : Body {

    val positionAndVelocity by lazy { computePositionAndVelocity(ra, dec, pmRA, pmDEC, parallax, radialVelocity) }

    // eraStarpv: wrong values when parallax is zero?
    // val positionAndVelocity by lazy { eraStarpv(ra, dec, pmRA, pmDEC, parallax, radialVelocity) }

    override val center = 0

    override val target = Int.MIN_VALUE

    override fun observedAt(observer: ICRF): PositionAndVelocity {
        // Form unit vector 'u1' in direction of star.
        val u1 = positionAndVelocity.position.normalized
        // Light-time returned is the projection of vector "pos_obs" onto the
        // unit vector "u1", divided by the speed of light.
        val lightTime = u1.dot(observer.position) / SPEED_OF_LIGHT_AU_DAY
        val position = (positionAndVelocity.position + positionAndVelocity.velocity *
                (observer.time.tdb.whole - epoch.tdb.whole + lightTime + observer.time.tdb.fraction - epoch.tdb.fraction) - observer.position)
        return PositionAndVelocity(position, observer.velocity - positionAndVelocity.velocity)
    }

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        val position =
            positionAndVelocity.position + positionAndVelocity.velocity * (time.tdb.whole - epoch.tdb.whole + time.tdb.fraction - epoch.tdb.fraction)
        return PositionAndVelocity(position, positionAndVelocity.velocity)
    }

    companion object {

        private val MIN_PARALLAX = 1.0E-6 * MILLIASEC2RAD

        private fun computePositionAndVelocity(
            ra: Angle, dec: Angle,
            pmRA: Angle = 0.0, pmDEC: Angle = 0.0,
            parallax: Angle = 0.0, radialVelocity: Velocity = 0.0,
        ): PositionAndVelocity {
            val plx = max(MIN_PARALLAX, parallax)
            // Computing the star's position as an ICRF position and velocity.
            val dist = 1.0 / sin(plx)
            val cra = cos(ra)
            val sra = sin(ra)
            val cdc = cos(dec)
            val sdc = sin(dec)

            val position = Vector3D(
                dist * cdc * cra,
                dist * cdc * sra,
                dist * sdc,
            )

            val rvInMetersPerSecond = radialVelocity.toMetersPerSecond

            // Compute Doppler factor, which accounts for change in light travel time to star.
            val k = 1.0 / (1.0 - rvInMetersPerSecond / SPEED_OF_LIGHT)

            // Convert proper motion and radial velocity to orthogonal
            // components of motion with units of au/day.

            val pmr = pmRA / (plx * DAYSPERJY) * k
            val pmd = pmDEC / (plx * DAYSPERJY) * k
            val rvl = rvInMetersPerSecond * DAYSEC / AU_M * k

            val velocity = Vector3D(
                -pmr * sra - pmd * sdc * cra + rvl * cdc * cra,
                pmr * cra - pmd * sdc * sra + rvl * cdc * sra,
                pmd * cdc + rvl * sdc,
            )

            return PositionAndVelocity(position, velocity)
        }
    }
}
