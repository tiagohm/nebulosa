package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.DeviceEvent

data class MountDetached(override val device: Mount) : DeviceEvent<Mount>
