package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserCanRelativeMoveChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
