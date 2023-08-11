package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.DeviceMessageReceived
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("serializer")
class DeviceMessageReceivedSerializer : StdSerializer<DeviceMessageReceived>(DeviceMessageReceived::class.java) {

    override fun serialize(
        event: DeviceMessageReceived,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("device", event.device?.name)
        gen.writeStringField("message", event.message)
        gen.writeEndObject()
    }
}
