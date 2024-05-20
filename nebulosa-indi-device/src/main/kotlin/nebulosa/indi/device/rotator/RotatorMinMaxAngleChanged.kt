package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorMinMaxAngleChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
