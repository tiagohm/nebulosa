package nebulosa.watney.plate.solving

import nebulosa.watney.plate.solving.quad.StarQuadMatch

internal data class SolveResult(
    @JvmField var success: Boolean = false,
    @JvmField var solution: ComputedPlateSolution? = null,
    @JvmField var searchRun: SearchRun? = null,
    @JvmField var numPotentialMatches: Int = 0,
    @JvmField var matchedQuads: List<StarQuadMatch> = emptyList(),
)
