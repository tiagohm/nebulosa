package nebulosa.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import java.nio.file.Path

class PathModule : SimpleModule() {

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        with(SimpleDeserializers()) {
            addDeserializer(Path::class.java, PathDeserializer())
            context.addDeserializers(this)
        }

        with(SimpleSerializers()) {
            addSerializer(PathSerializer())
            context.addSerializers(this)
        }
    }
}
