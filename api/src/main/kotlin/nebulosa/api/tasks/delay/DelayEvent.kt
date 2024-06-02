package nebulosa.api.tasks.delay

import java.time.Duration

data class DelayEvent(
    @JvmField val task: DelayTask,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val waitTime: Duration = Duration.ZERO,
    @JvmField val progress: Double = 0.0,
)
