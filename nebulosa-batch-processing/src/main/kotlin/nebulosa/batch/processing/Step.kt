package nebulosa.batch.processing

interface Step : Stoppable {

    fun execute(stepExecution: StepExecution): StepResult

    override fun stop(mayInterruptIfRunning: Boolean) = Unit
}
