package nebulosa.api.services

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraEvent
import org.springframework.stereotype.Service

@Service
class CameraService : ArrayList<Camera>(2) {

    internal fun onCameraEventReceived(event: CameraEvent) {

    }
}
