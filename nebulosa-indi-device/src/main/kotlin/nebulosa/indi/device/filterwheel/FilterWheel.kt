package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType

interface FilterWheel : Device {

    override val type
        get() = DeviceType.WHEEL

    val count: Int

    val position: Int

    val moving: Boolean

    val names: List<String>

    fun moveTo(position: Int)

    fun names(names: Iterable<String>)
}
