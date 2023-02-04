package nebulosa.nova.position

import nebulosa.erfa.CartesianCoordinate
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Vector3D
import nebulosa.nova.frame.Frame
import nebulosa.time.InstantOfTime

/**
 * An apparent |xyz| position relative to a particular observer.
 *
 * This class’s vectors provide the position and velocity of a body
 * relative to an observer, adjusted to predict where the body’s image
 * will really appear (hence "apparent") in the sky:
 *
 * * Light-time delay, as already present in an [Astrometric] position.
 * * Deflection: gravity bends light, and thus the image of a distant
 * object, as the light passes massive objects like Jupiter, Saturn,
 * and the Sun.  For an observer on the Earth’s surface or in Earth
 * orbit, the slight deflection by the gravity of the Earth itself is
 * also included.
 *
 * * Aberration: incoming light arrives slanted because of the
 * observer's motion through space.
 */
class Apparent internal constructor(
    position: Vector3D,
    velocity: Vector3D,
    time: InstantOfTime,
    center: Number,
    target: Number,
) : ICRF(position, velocity, time, center, target) {

    companion object {

        /**
         * Generates an [Apparent] position from an [altitude] and [azimuth].
         */
        @JvmStatic
        fun altAz(
            position: ICRF,
            azimuth: Angle,
            altitude: Angle,
            distance: Distance = 0.1.au,
        ): Apparent {
            val frame = position.target as? Frame ?: throw IllegalArgumentException(
                "to compute an altazimuth position, you must observe from " +
                        "a specific Earth location or from a position on another body loaded from a set " +
                        "of planetary constants"
            )

            val r = frame.rotationAt(position.time)
            val p = r.transposed * CartesianCoordinate.of(azimuth, altitude, distance).vector3D

            return Apparent(p, Vector3D.EMPTY, position.time, position.center, position.target)
        }
    }
}
