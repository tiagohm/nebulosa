package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserCanReverseChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
