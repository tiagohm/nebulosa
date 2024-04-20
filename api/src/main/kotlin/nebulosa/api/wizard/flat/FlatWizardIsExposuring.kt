package nebulosa.api.wizard.flat

import java.time.Duration

data class FlatWizardIsExposuring(
    override val exposureTime: Duration,
    override val capture: CameraCaptureElapsed,
) : FlatWizardElapsed {

    override val state = FlatWizardState.EXPOSURING
    override val savedPath = null
    override val message = ""
}
