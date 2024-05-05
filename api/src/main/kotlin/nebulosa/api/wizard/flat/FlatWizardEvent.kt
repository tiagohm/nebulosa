package nebulosa.api.wizard.flat

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import java.nio.file.Path
import java.time.Duration

data class FlatWizardEvent(
    @JvmField val state: FlatWizardState = FlatWizardState.IDLE,
    @JvmField val exposureTime: Duration = Duration.ZERO,
    @JvmField val capture: CameraCaptureEvent? = null,
    @JvmField val savedPath: Path? = null,
) : MessageEvent {

    override val eventName = "FLAT_WIZARD.ELAPSED"
}
