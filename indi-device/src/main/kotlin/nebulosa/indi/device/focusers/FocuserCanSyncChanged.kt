package nebulosa.indi.device.focusers

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanSyncChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
