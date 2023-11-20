package nebulosa.watney.plate.solving

import nebulosa.constants.TAU
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.watney.plate.solving.BlindSearchStrategyOptions.DecSearchOrder.NORTH_FIRST
import nebulosa.watney.plate.solving.BlindSearchStrategyOptions.DecSearchOrder.SOUTH_FIRST
import nebulosa.watney.plate.solving.BlindSearchStrategyOptions.RaSearchOrder.EAST_FIRST
import nebulosa.watney.plate.solving.BlindSearchStrategyOptions.RaSearchOrder.WEST_FIRST
import kotlin.math.ceil
import kotlin.math.cos

data class BlindSearchStrategy(private val options: BlindSearchStrategyOptions = BlindSearchStrategyOptions.DEFAULT) : SearchStrategy {

    private val densityOffsets = IntArray(options.maxNegativeDensityOffset + options.maxPositiveDensityOffset + 1)

    init {
        ((-options.maxNegativeDensityOffset)..options.maxPositiveDensityOffset)
            .forEachIndexed { n, i -> densityOffsets[n] = i }
    }

    override fun searchQueue(): List<SearchRun> {
        val searchRuns = ArrayList<SearchRun>()
        var radius = options.startRadius.toDegrees
        val minRadius = options.minRadius.toDegrees

        while (radius >= minRadius) {
            // 4 iterations: positive and negative on east side,
            // positive and negative on west side.
            for (decIteration in 0..3) {
                var n = 0
                var declination = 0.0
                var complete = false

                while (!complete) {
                    if (declination >= 90.0) {
                        declination = 90.0
                        complete = true
                    }

                    val angularDistToCover = cos(declination.deg) * 180.0
                    val numberOfSearchCircles = ceil(angularDistToCover / (2 * radius)).toInt() + 1

                    val raStep = 180.0 / numberOfSearchCircles
                    val raOffset = n % 2 * 0.5 * raStep

                    // Adjust dec sign depending on which iteration we're on and what search ordering preference was used.
                    val adjustedDEC = if (options.searchOrderDEC == SOUTH_FIRST && decIteration < 2
                        || options.searchOrderDEC == NORTH_FIRST && decIteration >= 2
                    ) -declination else declination

                    repeat(numberOfSearchCircles) {
                        val ra = raOffset + it * raStep

                        val adjustedRA = if (options.searchOrderRA == WEST_FIRST && decIteration % 2 == 0
                            || options.searchOrderRA == EAST_FIRST && decIteration % 2 == 1
                        ) ra + 180.0 else ra

                        searchRuns.add(SearchRun(options.startRadius, adjustedRA.deg, adjustedDEC.deg, densityOffsets))
                    }

                    declination += radius
                    n++
                }
            }

            radius /= 2
        }

        return searchRuns
    }

    fun slice(slices: Int): List<SearchStrategy> {
        val sliceStrategies = ArrayList<PartialBlindSearchStrategy>(slices)

        repeat(slices) { sliceStrategies.add(PartialBlindSearchStrategy()) }

        val raSliceWidth = TAU / slices

        for (searchRun in searchQueue()) {
            for (i in 0 until slices) {
                val sectorStart = i * raSliceWidth
                val sectorEnd = sectorStart + raSliceWidth

                if (searchRun.centerRA in sectorStart..sectorEnd) {
                    sliceStrategies[i].searchRuns.add(searchRun)
                    break
                }
            }
        }

        return sliceStrategies
    }
}
