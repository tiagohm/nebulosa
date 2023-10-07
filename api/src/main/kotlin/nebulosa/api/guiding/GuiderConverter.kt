package nebulosa.api.guiding

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.guiding.Guider
import nebulosa.json.ToJson
import org.springframework.stereotype.Component

@Component
class GuiderConverter : ToJson<Guider> {

    override val type = Guider::class.java

    override fun serialize(value: Guider, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("state", value.state.name)
        gen.writeBooleanField("settling", value.isSettling)
        gen.writeNumberField("settlePixels", value.settlePixels)
        gen.writeNumberField("settleTime", value.settleTime.toMillis())
        gen.writeNumberField("settleTimeout", value.settleTimeout.toMillis())
        gen.writeEndObject()
    }
}
