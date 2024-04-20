package nebulosa.api.guiding

import java.time.Duration

sealed interface WaitForSettleEvent {

    val task: WaitForSettleTask

    data class Started(override val task: WaitForSettleTask) : WaitForSettleEvent

    data class Finished(
        override val task: WaitForSettleTask,
        @JvmField val elapsedTime: Duration
    ) : WaitForSettleEvent
}
