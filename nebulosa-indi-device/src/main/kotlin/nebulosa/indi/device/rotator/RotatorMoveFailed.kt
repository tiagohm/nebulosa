package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorMoveFailed(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
