package nebulosa.watney.platesolver

interface SearchStrategy {

    fun searchQueue(): List<SearchRun>
}
