package nebulosa.api.wizard.flat

import nebulosa.indi.device.camera.Camera
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
    fun startCapture(camera: Camera, request: FlatWizardRequest) {
        val savePath = request.capture.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesPath", camera.name, "FLAT")

        val capture = request.capture.copy(savePath = savePath)
        flatWizardExecutor.execute(camera, request.copy(capture = capture))
    }

    @Synchronized
    fun stopCapture(camera: Camera) {
        flatWizardExecutor.stop(camera)
    }
}
