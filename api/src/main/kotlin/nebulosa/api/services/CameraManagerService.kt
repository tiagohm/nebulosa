package nebulosa.api.services

import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.events.CameraAttachedEvent
import nebulosa.indi.devices.events.CameraDetachedEvent
import nebulosa.indi.devices.events.CameraEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CameraManagerService {

    private val cameras = ConcurrentHashMap<String, Camera>()

    @EventListener
    fun onCameraEventReceived(event: CameraEvent) {
        if (event is CameraAttachedEvent) {
            cameras[event.device.name] = event.device
        } else if (event is CameraDetachedEvent) {
            cameras.remove(event.device.name)
        }
    }
}
