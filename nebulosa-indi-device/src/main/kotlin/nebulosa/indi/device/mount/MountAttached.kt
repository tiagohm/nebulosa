package nebulosa.indi.device.mount

import nebulosa.indi.device.DeviceAttached

data class MountAttached(override val device: Mount) : MountEvent, DeviceAttached<Mount>
