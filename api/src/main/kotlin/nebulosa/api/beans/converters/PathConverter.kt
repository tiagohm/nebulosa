package nebulosa.api.beans.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.json.modules.FromJson
import nebulosa.json.modules.ToJson
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class PathConverter : ToJson<Path>, FromJson<Path> {

    override val type = Path::class.java

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Path? {
        return p.valueAsString?.let(Path::of)
    }

    override fun serialize(value: Path, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString("$value")
    }
}
