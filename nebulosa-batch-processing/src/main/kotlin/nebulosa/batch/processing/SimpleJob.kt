package nebulosa.batch.processing

abstract class SimpleJob : Job, ArrayList<Step> {

    constructor(initialCapacity: Int = 4) : super(initialCapacity)

    constructor(steps: Collection<Step>) : super(steps)

    constructor(vararg steps: Step) : this(steps.toList())

    @Volatile private var position = 0

    override fun hasNext(jobExecution: JobExecution): Boolean {
        return position < size
    }

    override fun next(jobExecution: JobExecution): Step {
        return this[position++]
    }

    override fun stop(mayInterruptIfRunning: Boolean) {
        if (position in indices) {
            this[position].stop(mayInterruptIfRunning)
        }
    }
}
