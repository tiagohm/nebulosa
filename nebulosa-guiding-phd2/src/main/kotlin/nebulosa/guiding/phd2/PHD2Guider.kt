package nebulosa.guiding.phd2

import nebulosa.guiding.Guider
import nebulosa.guiding.SiderealShiftTrackingRate
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount

class PHD2Guider(
    host: String,
    port: Int = 4400,
    override val mount: Mount,
    override val camera: Camera,
    override val guideOutput: GuideOutput,
) : Guider {

    private val client = PHD2Client(host, port)

    override val canClearCalibration: Boolean
        get() = TODO("Not yet implemented")

    override var shiftRate: SiderealShiftTrackingRate
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun autoSelectGuideStar() {
        TODO("Not yet implemented")
    }

    override fun start(forceCalibration: Boolean) {
        TODO("Not yet implemented")
    }

    override fun dither() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun clearCalibration() {
        TODO("Not yet implemented")
    }

    override fun stopShifting() {
        TODO("Not yet implemented")
    }
}
