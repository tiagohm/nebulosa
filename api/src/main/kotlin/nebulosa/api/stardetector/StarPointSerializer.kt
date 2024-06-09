package nebulosa.api.stardetector

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.stardetector.StarPoint
import org.springframework.stereotype.Component

@Component
class StarPointSerializer : StdSerializer<StarPoint>(StarPoint::class.java) {

    override fun serialize(star: StarPoint?, gen: JsonGenerator, provider: SerializerProvider) {
        if (star == null) gen.writeNull()
        else {
            gen.writeStartObject()
            gen.writeNumberField("x", star.x)
            gen.writeNumberField("y", star.y)
            gen.writeNumberField("hfd", star.hfd)
            gen.writeNumberField("snr", star.snr)
            gen.writeNumberField("flux", star.flux)
            gen.writeEndObject()
        }
    }
}
