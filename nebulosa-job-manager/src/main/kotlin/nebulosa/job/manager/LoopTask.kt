package nebulosa.job.manager

data class LoopTask(
    private val job: Job,
    private val tasks: List<Task>,
    private val condition: (Task, Int) -> Boolean = INFINITY_LOOP,
) : Task {

    @Volatile private var index = 0
    @Volatile private var count = 0

    @Volatile var state = TaskExecutionState.OK
        private set

    override fun run() {
        var prev: Task? = null

        while (job.isRunning && !job.isCancelled) {
            val task = tasks[index]

            if (!condition(task, count)) {
                break
            }

            state = job.runTask(task, prev)

            if (state == TaskExecutionState.OK) {
                prev = task
            } else if (state == TaskExecutionState.BREAK) {
                break
            }

            if (++index >= tasks.size) {
                index = 0
                count++
            }
        }
    }

    companion object {

        @JvmField val INFINITY_LOOP: (Task, Int) -> Boolean = { _, _ -> true }
    }
}
