package nebulosa.api.mounts

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.mount.Mount
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MountDeserializer : DeviceDeserializer<Mount>(Mount::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().mount(name)
}
