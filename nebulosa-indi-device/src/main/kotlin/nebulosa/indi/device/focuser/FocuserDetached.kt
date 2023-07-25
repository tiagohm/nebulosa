package nebulosa.indi.device.focuser

import nebulosa.indi.device.DeviceDetached

data class FocuserDetached(override val device: Focuser) : FocuserEvent, DeviceDetached<Focuser>
