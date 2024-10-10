package nebulosa.api.lightboxes

import nebulosa.indi.device.lightbox.LightBox

class LightBoxService(private val lightBoxEventHub: LightBoxEventHub) {

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

    fun listen(lightBox: LightBox) {
        lightBoxEventHub.listen(lightBox)
    }
}
