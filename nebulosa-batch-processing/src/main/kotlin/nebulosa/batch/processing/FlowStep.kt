package nebulosa.batch.processing

interface FlowStep : Step, Iterable<Step> {

    override fun execute(stepExecution: StepExecution): StepResult {
        return StepResult.FINISHED
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        forEach { it.stop(mayInterruptIfRunning) }
    }
}
