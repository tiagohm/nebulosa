package nebulosa.watney.platesolver

import nebulosa.watney.platesolver.quad.StarQuadMatch

internal data class SolveResult(
    @JvmField var success: Boolean = false,
    @JvmField var solution: ComputedPlateSolution? = null,
    @JvmField var searchRun: SearchRun? = null,
    @JvmField var numPotentialMatches: Int = 0,
    @JvmField var matchedQuads: List<StarQuadMatch> = emptyList(),
)
