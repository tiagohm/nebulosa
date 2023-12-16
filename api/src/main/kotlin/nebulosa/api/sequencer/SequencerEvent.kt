package nebulosa.api.sequencer

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent

data class SequencerEvent(
    val id: Int,
    val capture: CameraCaptureEvent? = null,
) : MessageEvent {

    override val eventName = "SEQUENCER_ELAPSED"
}
