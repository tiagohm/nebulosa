package nebulosa.desktop.logic

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent

interface DevicePropertyListener<in D : Device> {

    fun onReset()

    fun onChanged(prev: D?, device: D)

    suspend fun onDeviceEvent(event: DeviceEvent<*>, device: D)

    suspend fun onDeviceConnected() = Unit

    suspend fun onDeviceDisconnected() = Unit
}
