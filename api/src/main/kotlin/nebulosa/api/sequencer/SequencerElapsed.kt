package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraCaptureElapsed
import nebulosa.api.messages.MessageEvent
import java.time.Duration

data class SequencerElapsed(
    val id: Int,
    val elapsedTime: Duration,
    val remainingTime: Duration,
    val progress: Double,
    val capture: CameraCaptureElapsed? = null,
) : MessageEvent {

    override val eventName = "SEQUENCER.ELAPSED"
}
