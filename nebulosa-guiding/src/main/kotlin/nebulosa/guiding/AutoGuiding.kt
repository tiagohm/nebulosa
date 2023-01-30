package nebulosa.guiding

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guider.Guider
import nebulosa.indi.device.mount.Mount

interface AutoGuiding {

    val mount: Mount

    val camera: Camera

    val guider: Guider

    val canClearCalibration: Boolean

    var shiftRate: SiderealShiftTrackingRate

    fun autoSelectGuideStar()

    fun start(forceCalibration: Boolean)

    fun dither()

    fun stop()

    fun clearCalibration()

    fun stopShifting()
}
