package nebulosa.api.wizard.flat

import java.nio.file.Path
import java.time.Duration

interface FlatWizardExecutionListener {

    fun onFlatCaptured(step: FlatWizardStep, savedPath: Path, duration: Duration)

    fun onFlatFailed(step: FlatWizardStep)
}
