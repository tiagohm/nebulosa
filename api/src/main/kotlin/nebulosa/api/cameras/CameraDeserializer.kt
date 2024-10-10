package nebulosa.api.cameras

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.camera.Camera
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class CameraDeserializer : DeviceDeserializer<Camera>(Camera::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().camera(name)
}
