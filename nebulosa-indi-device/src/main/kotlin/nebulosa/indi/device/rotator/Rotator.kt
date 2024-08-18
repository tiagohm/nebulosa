package nebulosa.indi.device.rotator

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType

interface Rotator : Device {

    override val type
        get() = DeviceType.ROTATOR

    val moving: Boolean

    val canAbort: Boolean

    val canHome: Boolean

    val canSync: Boolean

    val canReverse: Boolean

    val reversed: Boolean

    val hasBacklashCompensation: Boolean

    val backslash: Int

    val angle: Double

    val minAngle: Double

    val maxAngle: Double

    fun moveRotator(angle: Double)

    fun syncRotator(angle: Double)

    fun homeRotator()

    fun reverseRotator(enable: Boolean)

    fun abortRotator()
}
