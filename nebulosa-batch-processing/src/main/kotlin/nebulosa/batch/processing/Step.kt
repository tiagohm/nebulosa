package nebulosa.batch.processing

interface Step : Stoppable, JobExecutionListener {

    fun execute(stepExecution: StepExecution): StepResult

    override fun stop(mayInterruptIfRunning: Boolean) = Unit
}
