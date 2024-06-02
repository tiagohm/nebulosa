package nebulosa.api.guiding

import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.guide.GuideOutput

fun interface GuideOutputEventAware {

    fun handleGuideOutputEvent(event: DeviceEvent<GuideOutput>)
}
