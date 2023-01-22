package nebulosa.nova.astrometry

import nebulosa.nasa.spk.SpkSegment
import nebulosa.time.InstantOfTime

/**
 * Computes the position and velocity of a body from [center] to [target] by Chebyshev polynomial.
 */
class ChebyshevPositionAndVelocity(private val segment: SpkSegment) : Body {

    override val center = segment.center

    override val target = segment.target

    override fun compute(time: InstantOfTime) = segment.compute(time)
}
