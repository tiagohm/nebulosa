package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import java.time.Duration

data class SequencerEvent(
    @JvmField val id: Int = 0,
    @JvmField val elapsedTime: Duration = Duration.ZERO,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val progress: Double = 0.0,
    @JvmField val capture: CameraCaptureEvent? = null,
) : MessageEvent {

    override val eventName = "SEQUENCER.ELAPSED"
}
