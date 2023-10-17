package nebulosa.api.wheels

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.json.ToJson
import org.springframework.stereotype.Component

@Component
class WheelConverter : ToJson<FilterWheel> {

    override val type = FilterWheel::class.java

    override fun serialize(value: FilterWheel, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeNumberField("count", value.count)
        gen.writeNumberField("position", value.position)
        gen.writeBooleanField("moving", value.moving)
        gen.writeEndObject()
    }
}
