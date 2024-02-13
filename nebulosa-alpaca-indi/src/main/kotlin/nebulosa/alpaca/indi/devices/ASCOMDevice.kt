package nebulosa.alpaca.indi.devices

import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.api.DeviceService
import nebulosa.indi.device.Device
import nebulosa.indi.device.PropertyVector

abstract class ASCOMDevice : Device {

    protected abstract val device: ConfiguredDevice
    protected abstract val service: DeviceService

    override val name
        get() = device.name

    override val properties = emptyMap<String, PropertyVector<*, *>>()
    override val messages = emptyList<String>()

    abstract fun refresh()
}
