package nebulosa.api.cameras

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.json.ToJson
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class CameraToJson(private val capturesPath: Path) : ToJson<Camera> {

    override val type = Camera::class.java

    override fun serialize(value: Camera, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
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
        gen.writeNumberField("exposureMin", value.exposureMin.inWholeMicroseconds)
        gen.writeNumberField("exposureMax", value.exposureMax.inWholeMicroseconds)
        gen.writeStringField("exposureState", value.exposureState.name)
        gen.writeNumberField("exposureTime", value.exposureTime.inWholeMicroseconds)
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
        gen.writeBooleanField("hasGuiderHead", value.hasGuiderHead)
        gen.writeNumberField("pixelSizeX", value.pixelSizeX)
        gen.writeNumberField("pixelSizeY", value.pixelSizeY)
        gen.writeBooleanField("canPulseGuide", value.canPulseGuide)
        gen.writeBooleanField("pulseGuiding", value.pulseGuiding)
        gen.writeBooleanField("hasThermometer", value.hasThermometer)
        gen.writeNumberField("temperature", value.temperature)
        gen.writeObjectField("capturesPath", Path.of("$capturesPath", value.name))
        gen.writeEndObject()
    }
}
