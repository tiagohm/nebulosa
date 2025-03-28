package nebulosa.api.wizard.flat

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class FlatWizardService(
    private val capturesPath: Path,
    private val flatWizardExecutor: FlatWizardExecutor,
) {

    @Synchronized
    fun start(camera: Camera, request: FlatWizardRequest, wheel: FilterWheel? = null) {
        val savePath = request.capture.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$capturesPath", camera.name, "FLAT")

        val capture = request.capture.copy(savePath = savePath)
        flatWizardExecutor.execute(camera, request.copy(capture = capture), wheel)
    }

    @Synchronized
    fun stop(camera: Camera) {
        flatWizardExecutor.stop(camera)
    }

    fun status(camera: Camera): FlatWizardEvent? {
        return flatWizardExecutor.status(camera)
    }
}
