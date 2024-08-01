package nebulosa.indi.device.dome

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType

interface Dome : Device {

    override val type
        get() = DeviceType.DOME
}
