package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserReverseChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
