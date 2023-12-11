package nebulosa.batch.processing

abstract class SimpleFlowStep : FlowStep {

    protected abstract val steps: Collection<Step>

    override fun execute(jobExecution: JobExecution): StepResult {
        return StepResult.FINISHED
    }

    final override fun iterator(): Iterator<Step> {
        return steps.iterator()
    }

    final override fun stop(mayInterruptIfRunning: Boolean) {
        super.stop(mayInterruptIfRunning)
    }
}
