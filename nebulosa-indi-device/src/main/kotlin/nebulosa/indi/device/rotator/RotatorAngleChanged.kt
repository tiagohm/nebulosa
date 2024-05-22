package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorAngleChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent