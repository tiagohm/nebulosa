package nebulosa.api.wizard.flat

import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class FlatWizardService(
    private val capturesPath: Path,
    private val flatWizardExecutor: FlatWizardExecutor,
) {

    @Synchronized
    fun startCapture(request: FlatWizardRequest) {
        val savePath = request.captureRequest.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesPath", request.captureRequest.camera!!.name, "FLAT")

        flatWizardExecutor.execute(request.copy(captureRequest = request.captureRequest.copy(savePath = savePath)))
    }
}
