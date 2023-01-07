package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserMovingChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
