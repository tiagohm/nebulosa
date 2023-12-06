package nebulosa.watney.plate.solving

import nebulosa.math.Angle

data class PointSearchStrategy(
    private val centerRA: Angle,
    private val centerDEC: Angle,
    private val options: PointSearchStrategyOptions = PointSearchStrategyOptions.DEFAULT,
) : SearchStrategy {

    private val densityOffsets = IntArray(options.maxNegativeDensityOffset + options.maxPositiveDensityOffset + 1)

    init {
        ((-options.maxNegativeDensityOffset)..options.maxPositiveDensityOffset)
            .forEachIndexed { n, i -> densityOffsets[n] = i }
    }

    override fun searchQueue(): List<SearchRun> {
        return listOf(SearchRun(0.0, centerRA, centerDEC, densityOffsets))
    }
}
