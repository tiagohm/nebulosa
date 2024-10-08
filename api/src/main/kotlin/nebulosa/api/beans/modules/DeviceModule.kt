package nebulosa.api.beans.modules

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import nebulosa.api.rotators.RotatorSerializer

class DeviceModule : SimpleModule() {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        val serializers = SimpleSerializers()
        serializers.addSerializer(RotatorSerializer())
        context.addSerializers(serializers)

        val deserializers = SimpleDeserializers()
        context.addDeserializers(deserializers)
    }
}
