package nebulosa.api.data.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.indi.device.camera.Camera
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("serializer")
class CameraSerializer : StdSerializer<Camera>(Camera::class.java) {

    override fun serialize(
        camera: Camera,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField("name", camera.name)
        gen.writeBooleanField("connected", camera.connected)
        gen.writeBooleanField("exposuring", camera.exposuring)
        gen.writeBooleanField("hasCoolerControl", camera.hasCoolerControl)
        gen.writeNumberField("coolerPower", camera.coolerPower)
        gen.writeBooleanField("cooler", camera.cooler)
        gen.writeBooleanField("hasDewHeater", camera.hasDewHeater)
        gen.writeBooleanField("dewHeater", camera.dewHeater)
        gen.writeArrayFieldStart("frameFormats")
        camera.frameFormats.forEach(gen::writeString)
        gen.writeEndArray()
        gen.writeBooleanField("canAbort", camera.canAbort)
        gen.writeNumberField("cfaOffsetX", camera.cfaOffsetX)
        gen.writeNumberField("cfaOffsetY", camera.cfaOffsetY)
        gen.writeStringField("cfaType", camera.cfaType.name)
        gen.writeNumberField("exposureMin", camera.exposureMin)
        gen.writeNumberField("exposureMax", camera.exposureMax)
        gen.writeStringField("exposureState", camera.exposureState.name)
        gen.writeNumberField("exposure", camera.exposure)
        gen.writeBooleanField("hasCooler", camera.hasCooler)
        gen.writeBooleanField("canSetTemperature", camera.canSetTemperature)
        gen.writeBooleanField("canSubFrame", camera.canSubFrame)
        gen.writeNumberField("x", camera.x)
        gen.writeNumberField("minX", camera.minX)
        gen.writeNumberField("maxX", camera.maxX)
        gen.writeNumberField("y", camera.y)
        gen.writeNumberField("minY", camera.minY)
        gen.writeNumberField("maxY", camera.maxY)
        gen.writeNumberField("width", camera.width)
        gen.writeNumberField("minWidth", camera.minWidth)
        gen.writeNumberField("maxWidth", camera.maxWidth)
        gen.writeNumberField("height", camera.height)
        gen.writeNumberField("minHeight", camera.minHeight)
        gen.writeNumberField("maxHeight", camera.maxHeight)
        gen.writeBooleanField("canBin", camera.canBin)
        gen.writeNumberField("maxBinX", camera.maxBinX)
        gen.writeNumberField("maxBinY", camera.maxBinY)
        gen.writeNumberField("binX", camera.binX)
        gen.writeNumberField("binY", camera.binY)
        gen.writeNumberField("gain", camera.gain)
        gen.writeNumberField("gainMin", camera.gainMin)
        gen.writeNumberField("gainMax", camera.gainMax)
        gen.writeNumberField("offset", camera.offset)
        gen.writeNumberField("offsetMin", camera.offsetMin)
        gen.writeNumberField("offsetMax", camera.offsetMax)
        gen.writeBooleanField("hasGuiderHead", camera.hasGuiderHead)
        gen.writeNumberField("pixelSizeX", camera.pixelSizeX)
        gen.writeNumberField("pixelSizeY", camera.pixelSizeY)
        gen.writeBooleanField("canPulseGuide", camera.canPulseGuide)
        gen.writeBooleanField("pulseGuiding", camera.pulseGuiding)
        gen.writeBooleanField("hasThermometer", camera.hasThermometer)
        gen.writeNumberField("temperature", camera.temperature)
        gen.writeEndObject()
    }
}
