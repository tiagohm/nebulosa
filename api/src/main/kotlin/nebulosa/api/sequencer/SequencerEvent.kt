package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.message.MessageEvent
import java.time.Duration

data class SequencerEvent(
    @JvmField val id: Int = 0,
    @JvmField val elapsedTime: Duration = Duration.ZERO,
    @JvmField val remainingTime: Duration = Duration.ZERO,
    @JvmField val progress: Double = 0.0,
    @JvmField val capture: CameraCaptureEvent? = null,
    @JvmField val state: SequencerState = SequencerState.IDLE,
) : MessageEvent {

    override val eventName = "SEQUENCER.ELAPSED"
}
