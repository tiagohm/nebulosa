package nebulosa.api.cameras

import nebulosa.api.devices.DeviceDeserializer
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.camera.Camera
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class CameraDeserializer : DeviceDeserializer<Camera>(Camera::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.camera(name)
}
