package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserMovingChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
