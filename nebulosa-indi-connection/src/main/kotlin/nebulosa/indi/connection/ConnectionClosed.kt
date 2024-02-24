package nebulosa.indi.connection

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.INDIDeviceProvider

data class ConnectionClosed(val provider: INDIDeviceProvider) : DeviceEvent<Device> {

    override val device = null
}
