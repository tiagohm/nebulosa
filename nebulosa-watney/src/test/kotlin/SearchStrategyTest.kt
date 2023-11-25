import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.watney.plate.solving.BlindSearchStrategy
import nebulosa.watney.plate.solving.BlindSearchStrategyOptions
import nebulosa.watney.plate.solving.NearbySearchStrategy
import nebulosa.watney.plate.solving.NearbySearchStrategyOptions

class SearchStrategyTest : StringSpec() {

    init {
        "blind" {
            var div = 128
            var i = 0

            val searchRunsCount = intArrayOf(898184, 226556, 57644, 14916, 3964, 1096, 308, 76)

            while (div >= 1) {
                val options = BlindSearchStrategyOptions(minRadius = (22.5 / div).deg)
                val strategy = BlindSearchStrategy(options)
                val searchRuns = strategy.searchQueue()
                searchRuns shouldHaveSize searchRunsCount[i++]
                div /= 2
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
