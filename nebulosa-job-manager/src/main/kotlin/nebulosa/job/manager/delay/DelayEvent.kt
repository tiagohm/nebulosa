package nebulosa.job.manager.delay

import nebulosa.job.manager.TaskEvent

sealed interface DelayEvent : TaskEvent {

    override val task: DelayTask

    val remainingTime: Long

    val elapsedTime: Long

    val waitTime: Long

    val progress: Double
}
