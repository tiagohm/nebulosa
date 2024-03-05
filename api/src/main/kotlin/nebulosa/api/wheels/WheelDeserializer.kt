package nebulosa.api.wheels

import nebulosa.api.beans.converters.device.DeviceDeserializer
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class WheelDeserializer : DeviceDeserializer<FilterWheel>(FilterWheel::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.wheel(name)
}
