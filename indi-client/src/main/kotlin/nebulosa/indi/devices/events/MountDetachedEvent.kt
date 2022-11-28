package nebulosa.indi.devices.events

import nebulosa.indi.devices.mounts.Mount

data class MountDetachedEvent(override val device: Mount) : DeviceEvent<Mount>
