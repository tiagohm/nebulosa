package nebulosa.api.rotators

import nebulosa.api.beans.converters.device.DeviceDeserializer
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.rotator.Rotator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class RotatorDeserializer : DeviceDeserializer<Rotator>(Rotator::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.rotator(name)
}
