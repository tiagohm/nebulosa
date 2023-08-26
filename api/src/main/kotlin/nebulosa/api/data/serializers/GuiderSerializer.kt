package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.guiding.GuidePoint
import nebulosa.guiding.Guider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("serializer")
class GuiderSerializer : StdSerializer<Guider>(Guider::class.java) {

    override fun serialize(
        guider: Guider,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeFieldName("lockPosition")
        gen.writeGuidePoint(guider.lockPosition)
        gen.writeFieldName("primaryStar")
        gen.writeGuidePoint(guider.primaryStar)
        gen.writeNumberField("searchRegion", guider.searchRegion)
        gen.writeBooleanField("looping", guider.isLooping)
        gen.writeBooleanField("calibrating", guider.isCalibrating)
        gen.writeBooleanField("guiding", guider.isGuiding)
        gen.writeEndObject()
    }

    companion object {

        @JvmStatic
        private fun JsonGenerator.writeGuidePoint(point: GuidePoint) {
            writeStartObject()
            writeNumberField("x", point.x)
            writeNumberField("y", point.y)
            writeBooleanField("valid", point.valid)
            writeEndObject()
        }
    }
}
