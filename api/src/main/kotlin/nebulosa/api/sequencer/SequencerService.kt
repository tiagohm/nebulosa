package nebulosa.api.sequencer

import nebulosa.indi.device.camera.Camera
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@Service
class SequencerService(
    private val sequencesPath: Path,
    private val sequencerExecutor: SequencerExecutor,
) {

    @Synchronized
    fun start(camera: Camera, request: SequencePlanRequest) {
        val savePath = request.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$sequencesPath", (System.currentTimeMillis() / 1000).toString())

        sequencerExecutor.execute(camera, request.copy(savePath = savePath))
    }

    @Synchronized
    fun stop(camera: Camera) {
        sequencerExecutor.stop(camera)
    }
}
