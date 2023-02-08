package nebulosa.nasa.spk

import nebulosa.erfa.PositionAndVelocity
import nebulosa.time.InstantOfTime

/**
 * Lagrange Interpolation - Unequal Time Steps.
 */
internal data class Type9Segment(
    override val spk: Spk,
    override val source: String,
    override val start: Double,
    override val end: Double,
    override val center: Int,
    override val target: Int,
    override val frame: Int,
    override val type: Int,
    override val startIndex: Int,
    override val endIndex: Int,
) : SpkSegment {

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        TODO("Not yet implemented")
    }
}
