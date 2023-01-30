package nebulosa.guiding.phd2

import nebulosa.guiding.AutoGuiding
import nebulosa.guiding.SiderealShiftTrackingRate

class PHD2AutoGuiding(
    host: String,
    port: Int = 4400,
) : AutoGuiding {

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
