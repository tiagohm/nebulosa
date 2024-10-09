package nebulosa.api.livestacker

import nebulosa.indi.device.camera.Camera
import nebulosa.livestacker.LiveStacker
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.deleteRecursively

class LiveStackingService {

    private val liveStackers = ConcurrentHashMap<Camera, Pair<LiveStacker, Path>>(2)

    fun start(camera: Camera, options: LiveStackingRequest) {
        stop(camera)

        val workingDirectory = Files.createTempDirectory("ls-")
        val liveStacker = options.get(workingDirectory)
        liveStackers[camera] = liveStacker to workingDirectory
        liveStacker.start()
    }

    fun add(camera: Camera, path: Path): Path? {
        return liveStackers[camera]?.first?.add(path)
    }

    fun stop(camera: Camera) {
        val (liveStacker, workingDirectory) = liveStackers.remove(camera) ?: return

        liveStacker.stop()
        workingDirectory.deleteRecursively()
    }
}
