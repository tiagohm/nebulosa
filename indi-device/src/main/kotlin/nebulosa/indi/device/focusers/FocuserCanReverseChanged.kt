package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanReverseChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
