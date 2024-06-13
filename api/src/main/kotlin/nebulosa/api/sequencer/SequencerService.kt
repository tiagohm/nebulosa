package nebulosa.api.sequencer

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
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
    fun start(
        camera: Camera, request: SequencePlanRequest,
        mount: Mount?, wheel: FilterWheel?, focuser: Focuser?, rotator: Rotator?,
    ) {
        val savePath = request.savePath
            ?.takeIf { "$it".isNotBlank() && it.exists() && it.isDirectory() }
            ?: Path.of("$sequencesPath", (System.currentTimeMillis() / 1000).toString())

        sequencerExecutor.execute(camera, request.copy(savePath = savePath), mount, wheel, focuser, rotator)
    }

    @Synchronized
    fun stop(camera: Camera) {
        sequencerExecutor.stop(camera)
    }

    fun status(camera: Camera): SequencerEvent? {
        return sequencerExecutor.status(camera)
    }
}
