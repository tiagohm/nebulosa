package nebulosa.api.wizard.flat

import java.time.Duration

data object FlatWizardFailed : FlatWizardElapsed {

    override val state = FlatWizardState.FAILED
    override val exposureTime: Duration = Duration.ZERO
    override val capture = null
    override val savedPath = null
    override val message = ""
}
