package nebulosa.indi.device.guider

import nebulosa.indi.device.PropertyChangedEvent

data class GuideOutputCanPulseGuideChanged(override val device: GuideOutput) : PropertyChangedEvent, GuideOutputEvent<GuideOutput>
