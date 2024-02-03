package nebulosa.batch.processing

import java.util.*

abstract class SimpleJob : Job, Iterable<Step> {

    private val steps = ArrayList<Step>()

    constructor(steps: Collection<Step>) {
        this.steps.addAll(steps)
    }

    constructor(vararg steps: Step) {
        steps.forEach(this.steps::add)
    }

    override val id = UUID.randomUUID().toString()

    @Volatile private var position = 0
    @Volatile private var end = false

    protected fun register(step: Step): Boolean {
        return steps.add(step)
    }

    protected fun unregister(step: Step): Boolean {
        return steps.remove(step)
    }

    protected fun clear() {
        return steps.clear()
    }

    override fun hasNext(jobExecution: JobExecution): Boolean {
        return !end && position < steps.size
    }

    override fun next(jobExecution: JobExecution): Step {
        check(!end) { "this job is ended" }
        return steps[position++]
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        if (end) return

        end = true

        if (position in 1..steps.size) {
            steps[position - 1].stop(mayInterruptIfRunning)
        }
    }

    fun reset() {
        end = false
        position = 0
    }

    override fun iterator(): Iterator<Step> {
        return steps.iterator()
    }

    override fun contains(data: Any): Boolean {
        return data is Step && data in steps
    }
}
