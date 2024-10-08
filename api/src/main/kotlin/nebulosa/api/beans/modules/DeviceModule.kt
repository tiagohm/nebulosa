package nebulosa.api.beans.modules

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import nebulosa.api.focusers.FocuserDeserializer
import nebulosa.api.focusers.FocuserSerializer
import nebulosa.api.rotators.RotatorDeserializer
import nebulosa.api.rotators.RotatorSerializer
import nebulosa.api.wheels.WheelDeserializer
import nebulosa.api.wheels.WheelSerializer
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.rotator.Rotator

class DeviceModule : SimpleModule() {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        val serializers = SimpleSerializers()
        serializers.addSerializer(RotatorSerializer())
        serializers.addSerializer(FocuserSerializer())
        serializers.addSerializer(WheelSerializer())
        context.addSerializers(serializers)

        val deserializers = SimpleDeserializers()
        deserializers.addDeserializer(Rotator::class.java, RotatorDeserializer())
        deserializers.addDeserializer(Focuser::class.java, FocuserDeserializer())
        deserializers.addDeserializer(FilterWheel::class.java, WheelDeserializer())
        context.addDeserializers(deserializers)
    }
}
