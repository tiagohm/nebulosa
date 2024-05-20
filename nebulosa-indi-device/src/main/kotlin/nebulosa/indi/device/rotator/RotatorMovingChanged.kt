package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorMovingChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
