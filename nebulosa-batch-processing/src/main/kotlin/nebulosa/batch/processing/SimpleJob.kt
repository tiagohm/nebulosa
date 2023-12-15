package nebulosa.batch.processing

abstract class SimpleJob : Job, ArrayList<Step> {

    constructor(initialCapacity: Int = 4) : super(initialCapacity)

    constructor(steps: Collection<Step>) : super(steps)

    constructor(vararg steps: Step) : this(steps.toList())

    @Volatile private var position = 0
    @Volatile private var stopped = false

    override fun hasNext(jobExecution: JobExecution): Boolean {
        return !stopped && position < size
    }

    override fun next(jobExecution: JobExecution): Step {
        return this[position++]
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        if (stopped) return

        stopped = true

        if (position in 1..size) {
            this[position - 1].stop(mayInterruptIfRunning)
        }
    }

    fun reset() {
        stopped = false
        position = 0
    }
}
