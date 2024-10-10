package nebulosa.api.focusers

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.focuser.Focuser

class FocuserSerializer : DeviceSerializer<Focuser>(Focuser::class.java) {

    override fun JsonGenerator.serialize(value: Focuser) {
        writeBooleanField("moving", value.moving)
        writeNumberField("position", value.position)
        writeBooleanField("canAbsoluteMove", value.canAbsoluteMove)
        writeBooleanField("canRelativeMove", value.canRelativeMove)
        writeBooleanField("canAbort", value.canAbort)
        writeBooleanField("canReverse", value.canReverse)
        writeBooleanField("reversed", value.reversed)
        writeBooleanField("canSync", value.canSync)
        writeBooleanField("hasBacklash", value.hasBacklash)
        writeNumberField("maxPosition", value.maxPosition)
        writeBooleanField("hasThermometer", value.hasThermometer)
        writeNumberField("temperature", value.temperature)
    }
}
