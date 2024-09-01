package nebulosa.job.manager.delay

import nebulosa.job.manager.Job

data class DelayStarted(override val job: Job, override val task: DelayTask) : DelayEvent {

    override val remainingTime = task.durationInMilliseconds
    override val elapsedTime = 0L
    override val waitTime = 0L
    override val progress = 0.0
}
