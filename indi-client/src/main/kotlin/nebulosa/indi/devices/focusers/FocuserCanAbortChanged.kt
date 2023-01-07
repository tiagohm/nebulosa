package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserCanAbortChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
