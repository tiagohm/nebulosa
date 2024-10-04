package nebulosa.api.cameras

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.GuideHead
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class CameraSerializer(private val capturesPath: Path) : DeviceSerializer<Camera>(Camera::class.java) {

    override fun JsonGenerator.serialize(value: Camera) {
        writeBooleanField("exposuring", value.exposuring)
        writeBooleanField("hasCoolerControl", value.hasCoolerControl)
        writeNumberField("coolerPower", value.coolerPower)
        writeBooleanField("cooler", value.cooler)
        writeBooleanField("hasDewHeater", value.hasDewHeater)
        writeBooleanField("dewHeater", value.dewHeater)
        writeObjectField("frameFormats", value.frameFormats)
        writeBooleanField("canAbort", value.canAbort)
        writeNumberField("cfaOffsetX", value.cfaOffsetX)
        writeNumberField("cfaOffsetY", value.cfaOffsetY)
        writeStringField("cfaType", value.cfaType.name)
        writeNumberField("exposureMin", value.exposureMin / 1000L)
        writeNumberField("exposureMax", value.exposureMax / 1000L)
        writeStringField("exposureState", value.exposureState.name)
        writeNumberField("exposureTime", value.exposureTime / 1000L)
        writeBooleanField("hasCooler", value.hasCooler)
        writeBooleanField("canSetTemperature", value.canSetTemperature)
        writeBooleanField("canSubFrame", value.canSubFrame)
        writeNumberField("x", value.x)
        writeNumberField("minX", value.minX)
        writeNumberField("maxX", value.maxX)
        writeNumberField("y", value.y)
        writeNumberField("minY", value.minY)
        writeNumberField("maxY", value.maxY)
        writeNumberField("width", value.width)
        writeNumberField("minWidth", value.minWidth)
        writeNumberField("maxWidth", value.maxWidth)
        writeNumberField("height", value.height)
        writeNumberField("minHeight", value.minHeight)
        writeNumberField("maxHeight", value.maxHeight)
        writeBooleanField("canBin", value.canBin)
        writeNumberField("maxBinX", value.maxBinX)
        writeNumberField("maxBinY", value.maxBinY)
        writeNumberField("binX", value.binX)
        writeNumberField("binY", value.binY)
        writeNumberField("gain", value.gain)
        writeNumberField("gainMin", value.gainMin)
        writeNumberField("gainMax", value.gainMax)
        writeNumberField("offset", value.offset)
        writeNumberField("offsetMin", value.offsetMin)
        writeNumberField("offsetMax", value.offsetMax)
        writeBooleanField("hasGuideHead", value.guideHead != null)
        writeNumberField("pixelSizeX", value.pixelSizeX)
        writeNumberField("pixelSizeY", value.pixelSizeY)
        writeBooleanField("canPulseGuide", value.canPulseGuide)
        writeBooleanField("pulseGuiding", value.pulseGuiding)
        writeBooleanField("hasThermometer", value.hasThermometer)
        writeNumberField("temperature", value.temperature)
        writeObjectField("capturesPath", Path.of("$capturesPath", value.name))

        if (value is GuideHead) {
            writeMainOrGuideHead(value.main, "main")
        } else if (value.guideHead != null) {
            writeMainOrGuideHead(value.guideHead!!, "guideHead")
        }
    }

    companion object {

        @JvmStatic
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
}
