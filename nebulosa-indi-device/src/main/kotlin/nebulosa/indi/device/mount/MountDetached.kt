package nebulosa.indi.device.mount

import nebulosa.indi.device.DeviceDetached

data class MountDetached(override val device: Mount) : MountEvent, DeviceDetached<Mount>
