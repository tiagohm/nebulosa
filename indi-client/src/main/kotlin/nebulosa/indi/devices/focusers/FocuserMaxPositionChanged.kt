package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserMaxPositionChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
