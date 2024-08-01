package nebulosa.api.cameras

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.GuideHead
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class CameraSerializer(private val capturesPath: Path) : StdSerializer<Camera>(Camera::class.java) {

    override fun serialize(value: Camera, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("type", value.type.name)
        gen.writeStringField("sender", value.sender.id)
        gen.writeStringField("id", value.id)
        gen.writeStringField("name", value.name)
        gen.writeBooleanField("connected", value.connected)
        gen.writeBooleanField("exposuring", value.exposuring)
        gen.writeBooleanField("hasCoolerControl", value.hasCoolerControl)
        gen.writeNumberField("coolerPower", value.coolerPower)
        gen.writeBooleanField("cooler", value.cooler)
        gen.writeBooleanField("hasDewHeater", value.hasDewHeater)
        gen.writeBooleanField("dewHeater", value.dewHeater)
        gen.writeObjectField("frameFormats", value.frameFormats)
        gen.writeBooleanField("canAbort", value.canAbort)
        gen.writeNumberField("cfaOffsetX", value.cfaOffsetX)
        gen.writeNumberField("cfaOffsetY", value.cfaOffsetY)
        gen.writeStringField("cfaType", value.cfaType.name)
        gen.writeNumberField("exposureMin", value.exposureMin.toNanos() / 1000L)
        gen.writeNumberField("exposureMax", value.exposureMax.toNanos() / 1000L)
        gen.writeStringField("exposureState", value.exposureState.name)
        gen.writeNumberField("exposureTime", value.exposureTime.toNanos() / 1000L)
        gen.writeBooleanField("hasCooler", value.hasCooler)
        gen.writeBooleanField("canSetTemperature", value.canSetTemperature)
        gen.writeBooleanField("canSubFrame", value.canSubFrame)
        gen.writeNumberField("x", value.x)
        gen.writeNumberField("minX", value.minX)
        gen.writeNumberField("maxX", value.maxX)
        gen.writeNumberField("y", value.y)
        gen.writeNumberField("minY", value.minY)
        gen.writeNumberField("maxY", value.maxY)
        gen.writeNumberField("width", value.width)
        gen.writeNumberField("minWidth", value.minWidth)
        gen.writeNumberField("maxWidth", value.maxWidth)
        gen.writeNumberField("height", value.height)
        gen.writeNumberField("minHeight", value.minHeight)
        gen.writeNumberField("maxHeight", value.maxHeight)
        gen.writeBooleanField("canBin", value.canBin)
        gen.writeNumberField("maxBinX", value.maxBinX)
        gen.writeNumberField("maxBinY", value.maxBinY)
        gen.writeNumberField("binX", value.binX)
        gen.writeNumberField("binY", value.binY)
        gen.writeNumberField("gain", value.gain)
        gen.writeNumberField("gainMin", value.gainMin)
        gen.writeNumberField("gainMax", value.gainMax)
        gen.writeNumberField("offset", value.offset)
        gen.writeNumberField("offsetMin", value.offsetMin)
        gen.writeNumberField("offsetMax", value.offsetMax)
        gen.writeBooleanField("hasGuideHead", value.guideHead != null)
        gen.writeNumberField("pixelSizeX", value.pixelSizeX)
        gen.writeNumberField("pixelSizeY", value.pixelSizeY)
        gen.writeBooleanField("canPulseGuide", value.canPulseGuide)
        gen.writeBooleanField("pulseGuiding", value.pulseGuiding)
        gen.writeBooleanField("hasThermometer", value.hasThermometer)
        gen.writeNumberField("temperature", value.temperature)
        gen.writeObjectField("capturesPath", Path.of("$capturesPath", value.name))

        if (value is GuideHead) {
            gen.writeMainOrGuideHead(value.main, "main")
        } else if (value.guideHead != null) {
            gen.writeMainOrGuideHead(value.guideHead!!, "guideHead")
        }

        gen.writeEndObject()
    }

    private fun JsonGenerator.writeMainOrGuideHead(camera: Camera, fieldName: String) {
        writeObjectFieldStart(fieldName)
        writeStringField("type", camera.type.name)
        writeStringField("id", camera.id)
        writeStringField("name", camera.name)
        writeStringField("sender", camera.sender.id)
        writeBooleanField("connected", camera.connected)
        writeEndObject()
    }
}
