package nebulosa.indi.device.guide

import nebulosa.indi.device.PropertyChangedEvent

data class GuideOutputPulsingChanged(override val device: GuideOutput) : PropertyChangedEvent, GuideOutputEvent<GuideOutput>
