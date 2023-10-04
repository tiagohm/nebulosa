package nebulosa.api.guiding

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.guiding.GuidePoint
import nebulosa.guiding.Guider
import nebulosa.json.ToJson
import org.springframework.stereotype.Component

@Component
class GuiderConverter : ToJson<Guider> {

    override val type = Guider::class.java

    override fun serialize(value: Guider, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeFieldName("lockPosition")
        gen.writeGuidePoint(value.lockPosition)
        gen.writeFieldName("primaryStar")
        gen.writeGuidePoint(value.primaryStar)
        gen.writeNumberField("searchRegion", value.searchRegion)
        // TODO: gen.writeBooleanField("looping", value.isLooping)
        gen.writeBooleanField("calibrating", value.isCalibrating)
        gen.writeBooleanField("guiding", value.isGuiding)
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
