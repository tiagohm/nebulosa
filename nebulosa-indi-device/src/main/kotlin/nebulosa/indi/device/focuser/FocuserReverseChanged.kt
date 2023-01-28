package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserReverseChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
