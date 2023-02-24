package nebulosa.astrometrynet.nova

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonFormat(shape = JsonFormat.Shape.NUMBER)
@JsonDeserialize(using = Parity.Deserializer::class)
enum class Parity {
    POSITIVE,
    NEGATIVE,
    BOTH;

    class Deserializer : JsonDeserializer<Parity>() {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Parity {
            return ENTRIES[p.valueAsDouble.toInt()]
        }
    }

    companion object {

        @JvmStatic private val ENTRIES = values()
    }
}
