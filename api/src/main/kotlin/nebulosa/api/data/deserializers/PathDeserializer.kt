package nebulosa.api.data.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
@Qualifier("deserializer")
class PathDeserializer : StdDeserializer<Path>(Path::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Path {
        return Path.of(p.valueAsString)
    }
}
