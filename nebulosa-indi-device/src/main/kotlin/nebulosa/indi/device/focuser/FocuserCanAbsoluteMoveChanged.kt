package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanAbsoluteMoveChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
