package nebulosa.api.wizard.flat

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import java.time.Duration

data class FlatWizardElapsed(
    val exposureTime: Duration,
    val capture: CameraCaptureEvent,
) : MessageEvent {

    override val eventName = "FLAT_WIZARD.ELAPSED"
}
