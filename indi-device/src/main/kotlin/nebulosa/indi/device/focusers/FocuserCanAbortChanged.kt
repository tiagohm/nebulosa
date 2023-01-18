package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanAbortChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
