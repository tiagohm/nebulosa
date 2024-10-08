package nebulosa.api.lightboxes

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.lightbox.LightBox
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class LightBoxDeserializer : DeviceDeserializer<LightBox>(LightBox::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().lightBox(name)
}
