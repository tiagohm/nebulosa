package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorReversedChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
