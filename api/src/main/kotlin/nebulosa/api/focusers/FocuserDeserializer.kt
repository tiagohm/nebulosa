package nebulosa.api.focusers

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.focuser.Focuser
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class FocuserDeserializer : DeviceDeserializer<Focuser>(Focuser::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().focuser(name)
}
