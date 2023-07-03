package nebulosa.api.components

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.stereotype.Component

@Component
class CameraManager : ArrayList<Camera>(2) {

    internal fun onCameraEventReceived(event: CameraEvent) {
        when (event) {
            is CameraAttached -> add(event.device)
            is CameraDetached -> remove(event.device)
        }
    }

    operator fun contains(name: String): Boolean {
        return any { it.name == name }
    }
}
