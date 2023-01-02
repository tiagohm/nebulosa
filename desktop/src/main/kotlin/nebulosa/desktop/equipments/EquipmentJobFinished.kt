package nebulosa.desktop.equipments

import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceEvent

data class EquipmentJobFinished(
    override val device: Device,
    val task: ThreadedTask?,
) : DeviceEvent<Device>
