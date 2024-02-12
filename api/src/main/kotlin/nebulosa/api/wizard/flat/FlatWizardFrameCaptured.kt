package nebulosa.api.wizard.flat

import java.nio.file.Path
import java.time.Duration

data class FlatWizardFrameCaptured(
    override val exposureTime: Duration,
    override val savedPath: Path,
) : FlatWizardElapsed {

    override val state = FlatWizardState.CAPTURED
    override val capture = null
    override val message = ""
}
