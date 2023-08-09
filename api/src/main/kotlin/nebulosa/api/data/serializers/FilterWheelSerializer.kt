package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("serializer")
class FilterWheelSerializer : StdSerializer<FilterWheel>(FilterWheel::class.java) {

    override fun serialize(
        filterWheel: FilterWheel,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("name", filterWheel.name)
        gen.writeBooleanField("connected", filterWheel.connected)
        gen.writeNumberField("count", filterWheel.count)
        gen.writeNumberField("position", filterWheel.position)
        gen.writeBooleanField("moving", filterWheel.moving)
        gen.writeEndObject()
    }
}
