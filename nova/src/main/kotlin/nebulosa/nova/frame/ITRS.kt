package nebulosa.nova.frame

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.erfa.eraC2teqx
import nebulosa.math.Matrix3D
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime

/**
 * The International Terrestrial Reference System (ITRS).
 *
 * This is the IAU standard for an Earth-centered Earth-fixed (ECEF)
 * coordinate system, anchored to the Earth’s crust and continents.
 * This reference frame combines three other reference frames: the
 * Earth’s true equator and equinox of date, the Earth’s rotation with
 * respect to the stars, and the polar wobble of the crust with respect
 * to the Earth’s pole of rotation.
 */
object ITRS : Frame {

    @JvmStatic private val EARTH_ANGULAR_VELOCITY_MATRIX = Matrix3D(
        0.0, DAYSEC * ANGULAR_VELOCITY, 0.0,
        -DAYSEC * ANGULAR_VELOCITY, 0.0, 0.0,
        0.0, 0.0, 0.0,
    )

    override fun rotationAt(time: InstantOfTime) = eraC2teqx(time.m, time.gast, time.polarMotionMatrix)

    // TODO: taking the derivative of the instantaneous angular velocity provides a more accurate transform.
    override fun dRdtTimesRtAt(time: InstantOfTime) = EARTH_ANGULAR_VELOCITY_MATRIX
}
