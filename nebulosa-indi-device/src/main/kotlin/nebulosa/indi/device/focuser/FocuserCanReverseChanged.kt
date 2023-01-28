package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanReverseChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
