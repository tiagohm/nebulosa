package nebulosa.guiding

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount

interface Guider {

    val mount: Mount

    val camera: Camera

    val guideOutput: GuideOutput

    val canClearCalibration: Boolean

    var shiftRate: SiderealShiftTrackingRate

    fun autoSelectGuideStar()

    fun start(forceCalibration: Boolean)

    fun dither()

    fun stop()

    fun clearCalibration()

    fun stopShifting()
}
