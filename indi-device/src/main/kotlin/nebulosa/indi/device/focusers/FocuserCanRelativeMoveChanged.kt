package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanRelativeMoveChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
