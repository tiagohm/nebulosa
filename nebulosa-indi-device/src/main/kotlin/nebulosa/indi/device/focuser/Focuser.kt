package nebulosa.indi.device.focuser

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceType
import nebulosa.indi.device.thermometer.Thermometer

interface Focuser : Device, Thermometer {

    override val type
        get() = DeviceType.FOCUSER

    val moving: Boolean

    val position: Int

    val canAbsoluteMove: Boolean

    val canRelativeMove: Boolean

    val canAbort: Boolean

    val canReverse: Boolean

    val reversed: Boolean

    val canSync: Boolean

    val hasBacklash: Boolean

    val maxPosition: Int

    fun moveFocusIn(steps: Int)

    fun moveFocusOut(steps: Int)

    fun moveFocusTo(steps: Int)

    fun abortFocus()

    fun reverseFocus(enable: Boolean)

    fun syncFocusTo(steps: Int)
}
