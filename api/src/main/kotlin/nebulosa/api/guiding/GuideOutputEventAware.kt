package nebulosa.api.guiding

import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.guider.GuideOutput

fun interface GuideOutputEventAware {

    fun handleGuideOutputEvent(event: DeviceEvent<GuideOutput>)
}
