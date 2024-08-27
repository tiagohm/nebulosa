package nebulosa.api.devices

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.Device

abstract class DeviceSerializer<T : Device>(type: Class<T>) : StdSerializer<T>(type) {

    protected abstract fun JsonGenerator.serialize(value: T)

    final override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("type", value.type.name)
        gen.writeStringField("sender", value.sender.id)
        gen.writeStringField("driverName", value.driverName)
        gen.writeStringField("driverVersion", value.driverVersion)
        gen.writeStringField("id", value.id)
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.serialize(value)
        gen.writeEndObject()
    }
}
