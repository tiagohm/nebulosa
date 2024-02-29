package nebulosa.batch.processing

import nebulosa.common.concurrency.latch.Pauseable
import java.util.*

abstract class SimpleJob : Job, Pauseable, Iterable<Step> {

    private val steps = ArrayList<Step>()

    constructor(steps: Collection<Step>) {
        this.steps.addAll(steps)
    }

    constructor(vararg steps: Step) {
        steps.forEach(this.steps::add)
    }

    override val id = UUID.randomUUID().toString()

    @Volatile private var position = 0
    @Volatile private var isEnded = false

    protected fun register(step: Step): Boolean {
        return steps.add(step)
    }

    protected fun unregister(step: Step): Boolean {
        return steps.remove(step)
    }

    protected fun clear() {
        return steps.clear()
    }

    final override fun hasNext(jobExecution: JobExecution): Boolean {
        return !isEnded && position < steps.size
    }

    final override fun next(jobExecution: JobExecution): Step {
        check(!isEnded) { "this job is ended" }
        return steps[position++]
    }

    final override fun stop(mayInterruptIfRunning: Boolean) {
        if (isEnded) return

        isEnded = true

        if (position in 1..steps.size) {
            steps[position - 1].stop(mayInterruptIfRunning)
        }
    }

    final override val isPaused
        get() = steps.any { it !== this && it is Pauseable && it.isPaused }

    final override fun pause() {
        if (isEnded) return

        if (position in 1..steps.size) {
            val step = steps[position - 1]

            if (step is Pauseable) {
                step.pause()
            }
        }
    }

    final override fun unpause() {
        if (isEnded) return

        if (position in 1..steps.size) {
            val step = steps[position - 1]

            if (step is Pauseable) {
                step.unpause()
            }
        }
    }

    fun reset() {
        isEnded = false
        position = 0
    }

    final override fun iterator(): Iterator<Step> {
        return steps.iterator()
    }

    override fun contains(data: Any): Boolean {
        return data is Step && data in steps
    }
}
