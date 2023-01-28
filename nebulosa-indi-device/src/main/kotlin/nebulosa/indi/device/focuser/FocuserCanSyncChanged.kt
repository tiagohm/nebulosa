package nebulosa.indi.device.focuser

import nebulosa.indi.device.PropertyChangedEvent

data class FocuserCanSyncChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
