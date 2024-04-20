package nebulosa.api.wizard.flat

import nebulosa.api.messages.MessageEvent
import java.nio.file.Path
import java.time.Duration

sealed interface FlatWizardElapsed : MessageEvent {

    val state: FlatWizardState

    val exposureTime: Duration

    val capture: CameraCaptureElapsed?

    val savedPath: Path?

    val message: String

    override val eventName
        get() = "FLAT_WIZARD.ELAPSED"
}
