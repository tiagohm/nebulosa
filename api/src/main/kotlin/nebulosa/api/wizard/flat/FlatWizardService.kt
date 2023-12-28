package nebulosa.api.wizard.flat

import org.springframework.stereotype.Service

@Service
class FlatWizardService(
    private val flatWizardExecutor: FlatWizardExecutor,
) {

    @Synchronized
    fun startCapture(request: FlatWizardRequest) {
        flatWizardExecutor.execute(request)
    }
}
