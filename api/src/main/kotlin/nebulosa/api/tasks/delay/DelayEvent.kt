package nebulosa.api.tasks.delay

import java.time.Duration

sealed interface DelayEvent {

    val task: DelayTask

    data class Elapsed(
        override val task: DelayTask,
        @JvmField val remainingTime: Duration,
        @JvmField val waitTime: Duration,
        @JvmField val progress: Double,
    ) : DelayEvent
}
