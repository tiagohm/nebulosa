package nebulosa.indi.device.dustcap

import nebulosa.indi.device.DeviceDetached

data class DustCapDetached(override val device: DustCap) : DustCapEvent, DeviceDetached<DustCap>
