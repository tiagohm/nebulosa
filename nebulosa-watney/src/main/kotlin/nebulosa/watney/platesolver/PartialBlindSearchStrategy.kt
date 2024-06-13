package nebulosa.watney.platesolver

internal data class PartialBlindSearchStrategy(internal val searchRuns: MutableList<SearchRun> = ArrayList()) : SearchStrategy {

    override fun searchQueue(): List<SearchRun> {
        return searchRuns
    }
}
