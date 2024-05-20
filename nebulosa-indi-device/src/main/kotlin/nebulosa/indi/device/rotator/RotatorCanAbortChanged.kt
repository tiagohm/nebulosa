package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorCanAbortChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
