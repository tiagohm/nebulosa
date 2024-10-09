package nebulosa.api.rotators

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.rotator.Rotator
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RotatorDeserializer : DeviceDeserializer<Rotator>(Rotator::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().rotator(name)
}
