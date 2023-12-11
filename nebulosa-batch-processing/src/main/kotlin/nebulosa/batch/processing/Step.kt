package nebulosa.batch.processing

interface Step : Stoppable {

    fun execute(jobExecution: JobExecution): StepResult

    override fun stop(mayInterruptIfRunning: Boolean) = Unit
}
