package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserMaxPositionChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
