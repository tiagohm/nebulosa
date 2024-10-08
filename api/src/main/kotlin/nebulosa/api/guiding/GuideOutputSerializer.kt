package nebulosa.api.guiding

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.guider.GuideOutput

class GuideOutputSerializer : DeviceSerializer<GuideOutput>(GuideOutput::class.java) {

    override fun JsonGenerator.serialize(value: GuideOutput) {
        writeBooleanField("canPulseGuide", value.canPulseGuide)
        writeBooleanField("pulseGuiding", value.pulseGuiding)
    }
}
