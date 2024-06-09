import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.math.toDegrees
import nebulosa.watney.platesolver.BlindSearchStrategy
import nebulosa.watney.platesolver.BlindSearchStrategyOptions
import nebulosa.watney.platesolver.NearbySearchStrategy
import nebulosa.watney.platesolver.NearbySearchStrategyOptions

class SearchStrategyTest : StringSpec() {

    init {
        "blind" {
            var div = 128
            var i = 0

            val searchRunSizes = intArrayOf(898184, 226556, 57644, 14916, 3964, 1096, 308, 76)
            val searchRunSizesByRadius = listOf(
                // @formatter:off
                listOf(22.5 to 76, 11.25 to 232, 5.625 to 788, 2.8125 to 2868, 1.40625 to 10952, 0.703125 to 42728, 0.3515625 to 168912, 0.17578125 to 671628),
                listOf(22.5 to 76, 11.25 to 232, 5.625 to 788, 2.8125 to 2868, 1.40625 to 10952, 0.703125 to 42728, 0.3515625 to 168912),
                listOf(22.5 to 76, 11.25 to 232, 5.625 to 788, 2.8125 to 2868, 1.40625 to 10952, 0.703125 to 42728),
                listOf(22.5 to 76, 11.25 to 232, 5.625 to 788, 2.8125 to 2868, 1.40625 to 10952),
                listOf(22.5 to 76, 11.25 to 232, 5.625 to 788, 2.8125 to 2868),
                listOf(22.5 to 76, 11.25 to 232, 5.625 to 788),
                listOf(22.5 to 76, 11.25 to 232),
                listOf(22.5 to 76),
                // @formatter:on
            )

            while (div >= 1) {
                val options = BlindSearchStrategyOptions(minRadius = (22.5 / div).deg)
                val strategy = BlindSearchStrategy(options)
                val searchRuns = strategy.searchQueue()
                searchRuns shouldHaveSize searchRunSizes[i]
                val groupedSearchRuns = searchRuns.groupBy { it.radius.toDegrees }

                searchRunSizesByRadius[i].forEach {
                    groupedSearchRuns shouldContainKey it.first
                    groupedSearchRuns[it.first]!! shouldHaveSize it.second
                }

                div /= 2
                i++
            }
        }
        "nearby" {
            val intermediateFieldRadiusSteps = intArrayOf(-1, -1, 0, 3)
            val minFieldRadii = intArrayOf(0, 1, 1, 1)
            val maxFieldRadii = intArrayOf(2, 8, 8, 8)
            val searchRunsCount = intArrayOf(39, 211, 158, 193)

            repeat(intermediateFieldRadiusSteps.size) {
                val options = NearbySearchStrategyOptions(10.deg, minFieldRadii[it].deg, maxFieldRadii[it].deg, intermediateFieldRadiusSteps[it])
                val strategy = NearbySearchStrategy("06 45 08.91728".hours, "-16 42 58.0171".deg, options)
                val queue = strategy.searchQueue()
                queue shouldHaveSize searchRunsCount[it]
            }
        }
    }
}
