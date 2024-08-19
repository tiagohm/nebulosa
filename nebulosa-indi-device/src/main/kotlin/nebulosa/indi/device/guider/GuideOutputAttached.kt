package nebulosa.indi.device.guider

import nebulosa.indi.device.DeviceAttached

data class GuideOutputAttached(override val device: GuideOutput) : GuideOutputEvent<GuideOutput>, DeviceAttached<GuideOutput>
