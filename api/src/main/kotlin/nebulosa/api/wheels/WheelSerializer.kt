package nebulosa.api.wheels

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.filterwheel.FilterWheel
import org.springframework.stereotype.Component

@Component
class WheelSerializer : DeviceSerializer<FilterWheel>(FilterWheel::class.java) {

    override fun JsonGenerator.serialize(value: FilterWheel) {
        writeNumberField("count", value.count)
        writeNumberField("position", value.position)
        writeBooleanField("moving", value.moving)
        writeArrayFieldStart("names")
        value.names.forEach(::writeString)
        writeEndArray()
    }
}
