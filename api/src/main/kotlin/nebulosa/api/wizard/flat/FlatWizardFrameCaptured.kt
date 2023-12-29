package nebulosa.api.wizard.flat

import nebulosa.api.messages.MessageEvent
import java.nio.file.Path
import java.time.Duration

data class FlatWizardFrameCaptured(
    val savedPath: Path,
    val exposureTime: Duration,
) : MessageEvent {

    override val eventName = "FLAT_WIZARD.FRAME_CAPTURED"
}
