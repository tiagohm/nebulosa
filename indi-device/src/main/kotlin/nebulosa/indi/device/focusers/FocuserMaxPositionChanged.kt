package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserMaxPositionChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
