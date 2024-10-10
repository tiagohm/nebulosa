package nebulosa.job.manager.delay

import nebulosa.job.manager.TimedTaskEvent

sealed interface DelayEvent : TimedTaskEvent {

    override val task: DelayTask

    val waitTime: Long
}
