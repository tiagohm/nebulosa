package nebulosa.api.scheduler

import java.time.LocalDateTime

data class ScheduledTaskRes(
    val name: String,
    val data: Map<String, Any?>,
    val progress: Double,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val running: Boolean,
    val finished: Boolean,
    val finishedWithError: Boolean,
) {

    companion object {

        @JvmStatic
        fun from(task: ScheduledTask<*>) = ScheduledTaskRes(
            task.name,
            task.data,
            task.progress,
            task.startedAt,
            task.finishedAt,
            task.isRunning(),
            task.isDone(),
            task.finishedWithError,
        )
    }
}
