package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
@Qualifier("serializer")
class PathSerializer : StdSerializer<Path>(Path::class.java) {

    override fun serialize(
        path: Path,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("path", "$path")
        gen.writeEndObject()
    }
}
