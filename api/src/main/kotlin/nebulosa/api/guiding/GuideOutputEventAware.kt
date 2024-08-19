package nebulosa.api.guiding

import nebulosa.indi.device.guider.GuideOutputEvent

fun interface GuideOutputEventAware {

    fun handleGuideOutputEvent(event: GuideOutputEvent<*>)
}
