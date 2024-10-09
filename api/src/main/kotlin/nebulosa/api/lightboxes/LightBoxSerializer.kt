package nebulosa.api.lightboxes

import com.fasterxml.jackson.core.JsonGenerator
import nebulosa.api.devices.DeviceSerializer
import nebulosa.indi.device.lightbox.LightBox

class LightBoxSerializer : DeviceSerializer<LightBox>(LightBox::class.java) {

    override fun JsonGenerator.serialize(value: LightBox) {
        writeBooleanField("enabled", value.enabled)
        writeNumberField("intensity", value.intensity)
        writeNumberField("maxIntensity", value.intensityMax)
        writeNumberField("minIntensity", value.intensityMin)
    }
}
