package nebulosa.nova.position

import nebulosa.math.Pressure
import nebulosa.math.Pressure.Companion.mbar
import nebulosa.math.Temperature
import nebulosa.math.Temperature.Companion.celsius
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.ICRF
import nebulosa.time.InstantOfTime

/**
 * An |xyz| vector between two instantaneous position.
 *
 * A geometric position is the difference between the Solar System
 * positions of two bodies at exactly the same instant.  It is not
 * corrected for the fact that, in real physics, it will take time for
 * light to travel from one position to the other.
 */
class Geometric internal constructor(
    position: Vector3D,
    velocity: Vector3D,
    time: InstantOfTime,
    center: Number,
    target: Number,
) : ICRF(position, velocity, time, center, target) {

    /**
     * Computes the altitude, azimuth and distance relative to the observer's horizon.
     */
    fun horizontal(
        temperature: Temperature = 10.0.celsius,
        pressure: Pressure = 1013.0.mbar,
    ) = horizontal(this, temperature, pressure)
}
