package nebulosa.api.autofocus

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.focuser.Focuser
import org.springframework.stereotype.Service

@Service
class AutoFocusService(
    private val autoFocusExecutor: AutoFocusExecutor,
) {

    fun start(camera: Camera, focuser: Focuser, body: AutoFocusRequest) {
        autoFocusExecutor.execute(camera, focuser, body)
    }
}
