package nebulosa.api.alignment.polar.darv

import nebulosa.api.messages.MessageEvent
import nebulosa.guiding.GuideDirection
import java.time.Duration

data class DARVEvent(
    @JvmField val id: String,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val direction: GuideDirection = GuideDirection.NORTH,
    @JvmField val progress: Double = 0.0,
    @JvmField val state: DARVState = DARVState.IDLE,
) : MessageEvent {

    override val eventName = "DARV.ELAPSED"
}
