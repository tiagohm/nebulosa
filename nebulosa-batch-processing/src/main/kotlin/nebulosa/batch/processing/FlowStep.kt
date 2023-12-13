package nebulosa.batch.processing

interface FlowStep : Step, StepExecutionListener, Collection<Step> {

    override fun execute(stepExecution: StepExecution): StepResult {
        return stepExecution.jobExecution.jobLauncher.stepHandler.handle(this, stepExecution)
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        forEach { it.stop(mayInterruptIfRunning) }
    }
}
