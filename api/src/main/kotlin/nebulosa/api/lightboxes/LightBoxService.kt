package nebulosa.api.lightboxes

import nebulosa.indi.device.lightbox.LightBox
import org.springframework.stereotype.Service

@Service
class LightBoxService {

    fun connect(lightBox: LightBox) {
        lightBox.connect()
    }

    fun disconnect(lightBox: LightBox) {
        lightBox.disconnect()
    }

    fun enable(lightBox: LightBox) {
        lightBox.enable()
    }

    fun disable(lightBox: LightBox) {
        lightBox.disable()
    }

    fun brightness(lightBox: LightBox, intensity: Double) {
        lightBox.brightness(intensity)
    }
}
