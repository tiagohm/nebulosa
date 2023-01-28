package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanAbortChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
