package nebulosa.api.guiding

import nebulosa.api.services.DeviceMessageEvent
import nebulosa.indi.device.guide.GuideOutput

data class GuideOutputMessageEvent(
    override val eventName: String,
    override val device: GuideOutput,
) : DeviceMessageEvent<GuideOutput>