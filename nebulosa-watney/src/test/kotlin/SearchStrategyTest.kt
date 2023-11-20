import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.watney.plate.solving.BlindSearchStrategy
import nebulosa.watney.plate.solving.NearbySearchStrategy

class SearchStrategyTest : StringSpec() {

    init {
        "blind" {
            val strategy = BlindSearchStrategy()
            val queue = strategy.searchQueue()
            queue shouldHaveSize 57644
        }
        "nearby" {
            val strategy = NearbySearchStrategy("06 45 08.91728".hours, "-16 42 58.0171".deg)
            val queue = strategy.searchQueue()
            queue shouldHaveSize 39
        }
    }
}
