package nebulosa.indi.devices.focusers

import nebulosa.indi.devices.PropertyChangedEvent

data class FocuserCanSyncChanged(override val device: Focuser) : FocuserEvent, PropertyChangedEvent
