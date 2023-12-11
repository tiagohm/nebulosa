package nebulosa.batch.processing

abstract class SimpleJob : Job {

    @Volatile private var position = 0

    protected abstract val steps: List<Step>

    override fun hasNext(jobExecution: JobExecution): Boolean {
        return position < steps.size
    }

    override fun next(jobExecution: JobExecution): Step {
        return steps[position++]
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        if (position in steps.indices) {
            steps[position].stop(mayInterruptIfRunning)
        }
    }
}
