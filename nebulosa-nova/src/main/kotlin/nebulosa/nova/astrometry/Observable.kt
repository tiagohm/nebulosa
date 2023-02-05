package nebulosa.nova.astrometry

import nebulosa.erfa.PositionAndVelocity
import nebulosa.nova.position.ICRF

/**
 * An observable object.
 */
interface Observable {

    /**
     * Computes the position of this body relative to the
     * Solar System Barycenter at [observer].
     */
    fun observe(observer: ICRF): PositionAndVelocity
}
