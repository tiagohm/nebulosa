package nebulosa.batch.processing

interface Step : Stoppable, JobExecutionListener {

    fun execute(stepExecution: StepExecution): StepResult

    fun executeSingle(stepExecution: StepExecution): StepResult {
        beforeJob(stepExecution.jobExecution)

        try {
            return execute(stepExecution)
        } finally {
            afterJob(stepExecution.jobExecution)
        }
    }

    override fun stop(mayInterruptIfRunning: Boolean) = Unit
}
