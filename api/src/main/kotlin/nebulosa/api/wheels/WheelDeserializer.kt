package nebulosa.api.wheels

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.filterwheel.FilterWheel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class WheelDeserializer : DeviceDeserializer<FilterWheel>(FilterWheel::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().wheel(name)
}
