package nebulosa.nova.position

import nebulosa.math.Vector3D
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
) : ICRF(position, velocity, time, center, target)
