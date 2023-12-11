package nebulosa.batch.processing

interface Stoppable {

    fun stop(mayInterruptIfRunning: Boolean = true)
}
