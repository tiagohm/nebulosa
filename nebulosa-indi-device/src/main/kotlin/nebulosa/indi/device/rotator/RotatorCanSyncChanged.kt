package nebulosa.indi.device.rotator

import nebulosa.indi.device.PropertyChangedEvent

data class RotatorCanSyncChanged(override val device: Rotator) : RotatorEvent, PropertyChangedEvent
