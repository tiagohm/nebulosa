package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanRelativeMoveChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
