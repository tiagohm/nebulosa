package nebulosa.api.alignment.polar.darv

import nebulosa.api.messages.MessageEvent
import nebulosa.guiding.GuideDirection
import java.time.Duration

sealed interface DARVEvent : MessageEvent {

    val id: String

    val remainingTime: Duration

    val progress: Double

    val direction: GuideDirection?

    val state: DARVState

    override val eventName
        get() = "DARV.ELAPSED"

    data class Started(
        override val id: String,
        override val remainingTime: Duration,
        override val direction: GuideDirection,
    ) : DARVEvent {

        override val progress = 0.0
        override val state = DARVState.INITIAL_PAUSE
    }

    data class Finished(
        override val id: String,
    ) : DARVEvent {

        override val remainingTime = Duration.ZERO!!
        override val progress = 0.0
        override val state = DARVState.IDLE
        override val direction = null
    }

    data class InitialPauseElapsed(
        override val id: String,
        override val remainingTime: Duration,
        override val progress: Double,
    ) : DARVEvent {

        override val state = DARVState.INITIAL_PAUSE
        override val direction = null
    }

    data class GuidePulseElapsed(
        override val id: String,
        override val remainingTime: Duration,
        override val progress: Double,
        override val direction: GuideDirection,
        override val state: DARVState,
    ) : MessageEvent, DARVEvent
}
