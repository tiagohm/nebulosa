package nebulosa.api.stardetection

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.star.detection.ImageStar
import org.springframework.stereotype.Component

@Component
class ImageStarSerializer : StdSerializer<ImageStar>(ImageStar::class.java) {

    override fun serialize(star: ImageStar?, gen: JsonGenerator, provider: SerializerProvider) {
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
