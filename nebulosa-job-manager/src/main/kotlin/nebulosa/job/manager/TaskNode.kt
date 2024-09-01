package nebulosa.job.manager

internal data class TaskNode(
    @JvmField val item: Task,
    @JvmField var prev: TaskNode? = null,
    @JvmField var next: TaskNode? = null,
)
