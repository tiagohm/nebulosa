package nebulosa.batch.processing

interface FlowStep : Step, Iterable<Step> {

    override fun stop(mayInterruptIfRunning: Boolean) {
        forEach { it.stop(mayInterruptIfRunning) }
    }
}
