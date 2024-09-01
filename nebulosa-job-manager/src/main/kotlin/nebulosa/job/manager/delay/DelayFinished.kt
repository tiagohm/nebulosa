package nebulosa.job.manager.delay

import nebulosa.job.manager.Job

data class DelayFinished(override val job: Job, override val task: DelayTask) : DelayEvent {

    override val remainingTime = 0L
    override val elapsedTime = task.durationInMilliseconds
    override val waitTime = 0L
    override val progress = 1.0
}
