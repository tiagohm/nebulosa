package nebulosa.nova.astrometry

import nebulosa.nasa.spk.SpkSegment
import nebulosa.time.InstantOfTime

/**
 * Computes the position of a body from [center] to [target] by Chebyshev polynomial.
 */
class ChebyshevPosition(private val segment: SpkSegment) : Body {

    override val center = segment.center

    override val target = segment.target

    override fun compute(time: InstantOfTime) = segment.compute(time)
}
