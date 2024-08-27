package nebulosa.api.lightboxes

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.lightbox.LightBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class LightBoxDeserializer : DeviceDeserializer<LightBox>(LightBox::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.lightBox(name)
}
