package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanAbsoluteMoveChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
