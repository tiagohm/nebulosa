package nebulosa.api.guiding

import nebulosa.api.connection.ConnectionService
import nebulosa.api.devices.DeviceDeserializer
import nebulosa.indi.device.guider.GuideOutput
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class GuideOutputDeserializer : DeviceDeserializer<GuideOutput>(GuideOutput::class.java), KoinComponent {

    override fun deviceFor(name: String) = get<ConnectionService>().guideOutput(name)
}
