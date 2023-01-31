package nebulosa.nova.astrometry

import nebulosa.math.Vector3D
import nebulosa.nova.position.ICRF

/**
 * An observable object.
 */
interface Observable {

    /**
     * Computes the position of this body relative to the
     * Solar System Barycenter at [observer].
     */
    fun observe(observer: ICRF): Pair<Vector3D, Vector3D>
}
