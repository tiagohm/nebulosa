package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserPositionChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
