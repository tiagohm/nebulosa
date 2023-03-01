package nebulosa.guiding

import java.io.Closeable

interface Guider : Closeable {

    val canClearCalibration: Boolean

    var shiftRate: SiderealShiftTrackingRate

    fun autoSelectGuideStar()

    fun start(forceCalibration: Boolean)

    fun dither()

    fun stop()

    fun clearCalibration()

    fun stopShifting()

    fun connect()
}
