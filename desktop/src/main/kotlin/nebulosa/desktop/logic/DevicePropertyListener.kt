package nebulosa.desktop.logic

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent

interface DevicePropertyListener<in D : Device> {

    fun onReset()

    fun onChanged(prev: D?, device: D)

    fun onDeviceEvent(event: DeviceEvent<*>, device: D)

    fun onDeviceConnected() = Unit

    fun onDeviceDisconnected() = Unit
}
