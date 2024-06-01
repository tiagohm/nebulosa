package nebulosa.api.autofocus

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.curve.fitting.CurvePoint
import org.springframework.stereotype.Component

@Component
class CurvePointSerializer : StdSerializer<CurvePoint>(CurvePoint::class.java) {

    override fun serialize(point: CurvePoint?, gen: JsonGenerator, provider: SerializerProvider) {
        if (point == null) gen.writeNull()
        else {
            gen.writeStartObject()
            gen.writeNumberField("x", point.x)
            gen.writeNumberField("y", point.y)
            gen.writeEndObject()
        }
    }
}
