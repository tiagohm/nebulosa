package nebulosa.api.focusers

import nebulosa.api.beans.converters.indi.DeviceDeserializer
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.focuser.Focuser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class FocuserDeserializer : DeviceDeserializer<Focuser>(Focuser::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.focuser(name)
}
