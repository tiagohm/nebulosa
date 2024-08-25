package nebulosa.indi.device.dustcap

import nebulosa.indi.device.DeviceEvent

interface DustCapEvent : DeviceEvent<DustCap> {

    override val device: DustCap
}
