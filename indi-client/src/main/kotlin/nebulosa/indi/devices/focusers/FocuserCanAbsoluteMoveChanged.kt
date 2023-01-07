package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserCanAbsoluteMoveChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
