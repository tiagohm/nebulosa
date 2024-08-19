package nebulosa.indi.device.focuser

import nebulosa.indi.device.DeviceEvent

interface FocuserEvent : DeviceEvent<Focuser> {

    override val device: Focuser
}
