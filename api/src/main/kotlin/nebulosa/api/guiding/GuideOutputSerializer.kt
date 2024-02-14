package nebulosa.api.guiding

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.guide.GuideOutput
import org.springframework.stereotype.Component

@Component
class GuideOutputSerializer : StdSerializer<GuideOutput>(GuideOutput::class.java) {

    override fun serialize(value: GuideOutput, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeBooleanField("canPulseGuide", value.canPulseGuide)
        gen.writeBooleanField("pulseGuiding", value.pulseGuiding)
        gen.writeEndObject()
    }
}
