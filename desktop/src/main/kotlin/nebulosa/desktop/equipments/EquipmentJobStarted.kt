package nebulosa.desktop.equipments

import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceEvent

data class EquipmentJobStarted(
    override val device: Device,
    val task: ThreadedTask?,
) : DeviceEvent<Device>
