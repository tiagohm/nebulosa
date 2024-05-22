package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorCanReverseChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
