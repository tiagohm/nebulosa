package nebulosa.nova.position

import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.ICRF
import nebulosa.time.InstantOfTime

/**
 * An |xyz| position measured from the center of the Earth.
 *
 * A geocentric position is the difference between the position of the
 * Earth at a given instant and the position of a target body at the
 * same instant, without accounting for light-travel time or the effect
 * of relativity on the light itself.
 */
class Geocentric internal constructor(
    position: Vector3D,
    velocity: Vector3D,
    time: InstantOfTime,
    center: Number,
    target: Number,
) : ICRF(position, velocity, time, center, target)
