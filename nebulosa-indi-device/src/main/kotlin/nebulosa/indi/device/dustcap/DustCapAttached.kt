package nebulosa.indi.device.dustcap

import nebulosa.indi.device.DeviceAttached

data class DustCapAttached(override val device: DustCap) : DustCapEvent, DeviceAttached<DustCap>
