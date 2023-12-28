package nebulosa.api.wizard.flat

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import java.time.Duration

data class FlatWizardElapsed(
    val duration: Duration,
    val captureEvent: CameraCaptureEvent,
) : MessageEvent {

    override val eventName = "FLAT_WIZARD_ELAPSED"
}
