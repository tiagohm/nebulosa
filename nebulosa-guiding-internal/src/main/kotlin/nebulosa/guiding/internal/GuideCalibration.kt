package nebulosa.guiding.internal

import nebulosa.constants.PI
import nebulosa.constants.PIOVERTWO
import nebulosa.math.Angle

class GuideCalibration(private val guider: MultiStarGuider) {

    private inline val mount: GuideMount?
        get() = guider.mount

    private var calibration = Calibration.EMPTY

    val xRate
        get() = calibration.xRate

    val yRate
        get() = calibration.yRate

    val declination
        get() = calibration.declination

    val rotatorAngle
        get() = calibration.rotatorAngle

    val binning
        get() = calibration.binning

    val pierSideAtEast
        get() = calibration.pierSideAtEast

    val raGuideParity
        get() = calibration.raGuideParity

    val decGuideParity
        get() = calibration.decGuideParity

    var yAngleError = Angle.ZERO
        private set

    var calibrated = false
        private set

    val xAngle
        get() = calibration.xAngle

    val yAngle
        // TODO: Check if normalization [-PI..PI] is correct.
        get() = (calibration.xAngle - yAngleError + PIOVERTWO).normalized - PI

    fun set(calibration: Calibration) {
        this.calibration.xRate = calibration.xRate
        this.calibration.yRate = calibration.yRate
        this.calibration.binning = calibration.binning
        this.calibration.declination = calibration.declination
        this.calibration.pierSideAtEast = calibration.pierSideAtEast
        if (calibration.raGuideParity != GuideParity.UNCHANGED)
            this.calibration.raGuideParity = calibration.raGuideParity
        if (calibration.decGuideParity != GuideParity.UNCHANGED)
            this.calibration.decGuideParity = calibration.decGuideParity
        this.calibration.rotatorAngle = calibration.rotatorAngle

        // The angles are more difficult because we have to turn yAngle into a yError.
        this.calibration.xAngle = calibration.xAngle
        this.calibration.yAngle = calibration.yAngle
        // TODO: Check if normalization [-PI..PI] is correct.
        yAngleError = (calibration.xAngle - calibration.yAngle + PIOVERTWO).normalized - PI

        calibrated = true
    }

    fun flip() {
        if (!calibrated) return

        val mount = mount ?: return

        val origX = xAngle
        val origY = yAngle

        val decFlipRequired = mount.calibrationFlipRequiresDecFlip

        var newX = origX + PI
        var newY = origY

        if (decFlipRequired) {
            newY += PI
        }

        // Normalize.
        // TODO: Check if normalization [-PI..PI] is correct.
        newX = newX.normalized - PI
        newY = newY.normalized - PI

        val pierSideAtEast = !calibration.pierSideAtEast
        // Dec polarity changes when pier side changes, i.e. if Guide(NORTH) moves the star north on one side,
        // then Guide(NORTH) will move the star south on the other side of the pier.
        // For mounts with calibrationFlipRequiresDecFlip, the parity does not change after the flip.
        val newDecParity = if (decFlipRequired) calibration.decGuideParity else calibration.decGuideParity.opposite

        set(calibration.copy(xAngle = newX, yAngle = newY, pierSideAtEast = pierSideAtEast, decGuideParity = newDecParity))
    }

    /*
     * Adjusts the calibration data for the scope's current coordinates.
     *
     * This includes adjusting the xRate to compensate for changes in declination
     * relative to the declination where calibration was done, and possibly flipping
     * the calibration data if the mount is known to be on the other side of the
     * pier from where calibration was done.
     */
    fun adjustCalibrationForScopePointing() {
        // TODO
    }
}
