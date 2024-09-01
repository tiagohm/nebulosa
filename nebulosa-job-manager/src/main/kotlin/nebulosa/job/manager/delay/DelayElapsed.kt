package nebulosa.job.manager.delay

import nebulosa.job.manager.Job

data class DelayElapsed(
    override val job: Job, override val task: DelayTask,
    override val remainingTime: Long,
    override val elapsedTime: Long,
    override val waitTime: Long,
    override val progress: Double,
) : DelayEvent
