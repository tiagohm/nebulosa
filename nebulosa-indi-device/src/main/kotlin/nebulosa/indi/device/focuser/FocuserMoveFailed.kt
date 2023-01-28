package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserMoveFailed(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
