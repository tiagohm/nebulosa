package nebulosa.api.autofocus

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.focuser.Focuser

class AutoFocusService(
    private val autoFocusExecutor: AutoFocusExecutor,
) {

    fun start(camera: Camera, focuser: Focuser, body: AutoFocusRequest) {
        autoFocusExecutor.execute(camera, focuser, body)
    }

    fun stop(camera: Camera) {
        autoFocusExecutor.stop(camera)
    }

    fun status(camera: Camera): AutoFocusEvent? {
        return autoFocusExecutor.status(camera)
    }
}
