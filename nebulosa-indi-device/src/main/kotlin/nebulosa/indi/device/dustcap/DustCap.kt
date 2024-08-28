package nebulosa.indi.device.dustcap

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.Parkable

interface DustCap : Device, Parkable {

    override val type
        get() = DeviceType.DUST_CAP
}
