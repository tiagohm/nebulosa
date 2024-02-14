package nebulosa.api.guiding

import nebulosa.api.beans.converters.indi.DeviceDeserializer
import nebulosa.api.connection.ConnectionService
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class GuideOutputDeserializer : DeviceDeserializer<GuideOutput>(GuideOutput::class.java) {

    @Autowired @Lazy private lateinit var connectionService: ConnectionService

    override fun deviceFor(name: String) = connectionService.guideOutput(name)
}
