package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserMoveFailed(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
