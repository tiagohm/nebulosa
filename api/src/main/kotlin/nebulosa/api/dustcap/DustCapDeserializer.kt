package nebulosa.api.dustcap

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.dustcap.DustCap
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DustCapDeserializer : DeviceDeserializer<DustCap>(DustCap::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().dustCap(name)
}
