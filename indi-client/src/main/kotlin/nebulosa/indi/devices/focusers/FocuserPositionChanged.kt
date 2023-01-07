package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserPositionChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
