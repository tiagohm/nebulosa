package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserReverseChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
