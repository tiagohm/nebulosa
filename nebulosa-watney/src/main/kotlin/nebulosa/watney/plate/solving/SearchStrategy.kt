package nebulosa.watney.plate.solving

interface SearchStrategy {

    fun searchQueue(): List<SearchRun>
}
