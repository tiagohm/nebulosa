import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.watney.plate.solving.BlindSearchStrategy

class BlindSearchStrategyTest : StringSpec() {

    init {
        "searchQueue" {
            val strategy = BlindSearchStrategy()
            val queue = strategy.searchQueue()
            queue shouldHaveSize 57644
        }
    }
}
