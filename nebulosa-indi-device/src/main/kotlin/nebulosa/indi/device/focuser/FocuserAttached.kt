package nebulosa.indi.device.focuser

import nebulosa.indi.device.DeviceAttached

data class FocuserAttached(override val device: Focuser) : FocuserEvent, DeviceAttached<Focuser>
