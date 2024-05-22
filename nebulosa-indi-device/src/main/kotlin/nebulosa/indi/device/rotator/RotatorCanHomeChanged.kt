package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorCanHomeChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
