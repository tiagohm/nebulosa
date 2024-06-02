package nebulosa.api.mounts

import nebulosa.api.devices.DeviceMessageEvent
import nebulosa.indi.device.mount.Mount

data class MountMessageEvent(override val eventName: String, override val device: Mount) : DeviceMessageEvent<Mount>
