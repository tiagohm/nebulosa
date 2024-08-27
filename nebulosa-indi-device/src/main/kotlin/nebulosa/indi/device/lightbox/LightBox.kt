package nebulosa.indi.device.lightbox

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType

interface LightBox : Device {

    override val type
        get() = DeviceType.LIGHT_BOX

    val enabled: Boolean

    val intensity: Double

    val intensityMin: Double

    val intensityMax: Double

    fun enable()

    fun disable()

    fun brightness(intensity: Double)
}
