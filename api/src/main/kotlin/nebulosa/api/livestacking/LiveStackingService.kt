package nebulosa.api.livestacking

import nebulosa.indi.device.camera.Camera
import nebulosa.livestacking.LiveStacker
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

@Service
class LiveStackingService {

    private val liveStackers = ConcurrentHashMap<Camera, LiveStacker>(2)

    fun start(camera: Camera, options: LiveStackingRequest) {
        stop(camera)

        val liveStacker = options.get()
        liveStackers[camera] = liveStacker
        liveStacker.start()
    }

    fun add(camera: Camera, path: Path): Path? {
        return liveStackers[camera]?.add(path)
    }

    fun stop(camera: Camera) {
        liveStackers.remove(camera)?.stop()
    }
}
