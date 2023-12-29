package nebulosa.api.beans.converters.indi

import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.mount.Mount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class MountDeserializer : DeviceDeserializer<Mount>(Mount::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.mount(name)
}
