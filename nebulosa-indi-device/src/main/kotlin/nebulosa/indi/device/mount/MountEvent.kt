package nebulosa.indi.device.mount

import nebulosa.indi.device.DeviceEvent

interface MountEvent : DeviceEvent<Mount> {

    override val device: Mount
}
