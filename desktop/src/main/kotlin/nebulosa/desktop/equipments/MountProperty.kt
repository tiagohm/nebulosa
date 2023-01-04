package nebulosa.desktop.equipments

import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.mounts.Mount

class MountProperty : DeviceProperty<Mount>() {

    override fun changed(value: Mount) {
    }

    override fun reset() {
    }

    override fun accept(event: DeviceEvent<*>) {
    }
}
