package nebulosa.api.lightboxes

import nebulosa.indi.device.lightbox.LightBoxEvent

fun interface LightBoxEventAware {

    fun handleLightBoxEvent(event: LightBoxEvent)
}
