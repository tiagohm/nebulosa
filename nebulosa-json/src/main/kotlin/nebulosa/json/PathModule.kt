package nebulosa.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import com.fasterxml.jackson.datatype.jsr310.PackageVersion
import java.nio.file.Path

class PathModule : SimpleModule(PackageVersion.VERSION) {

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
