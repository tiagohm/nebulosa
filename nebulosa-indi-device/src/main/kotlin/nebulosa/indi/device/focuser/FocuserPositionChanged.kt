package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserPositionChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
