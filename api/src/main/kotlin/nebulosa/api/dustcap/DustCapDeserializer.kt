package nebulosa.api.dustcap

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.dustcap.DustCap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class DustCapDeserializer : DeviceDeserializer<DustCap>(DustCap::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.dustCap(name)
}
