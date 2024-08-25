package nebulosa.indi.device.guider

import nebulosa.indi.device.DeviceDetached

data class GuideOutputDetached(override val device: GuideOutput) : GuideOutputEvent, DeviceDetached<GuideOutput>
