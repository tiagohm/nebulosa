package nebulosa.nova.position

import nebulosa.math.Pressure
import nebulosa.math.Pressure.Companion.mbar
import nebulosa.math.Temperature
import nebulosa.math.Temperature.Companion.celsius
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.ICRF
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
    val barycenter: Barycentric,
) : ICRF(position, velocity, time, center, target) {

    /**
     * Computes the altitude, azimuth and distance relative to the observer's horizon.
     */
    fun horizontal(
        temperature: Temperature = 10.0.celsius,
        pressure: Pressure = 1013.0.mbar,
    ) = horizontal(this, temperature, pressure)
}
