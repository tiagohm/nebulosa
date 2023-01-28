package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserMovingChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
