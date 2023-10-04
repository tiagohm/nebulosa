package nebulosa.guiding.internal

import nebulosa.imaging.Image

interface GuiderListener {

    fun onLockPositionChanged(position: GuidePoint)

    fun onStarSelected(star: StarPoint)

    fun onGuidingDithered(dx: Double, dy: Double)

    fun onGuidingStopped()

    fun onLockShiftLimitReached()

    fun onLooping(image: Image, number: Int, star: StarPoint?)

    fun onStarLost()

    fun onLockPositionLost()

    fun onStartCalibration()

    fun onCalibrationStep(
        calibrationState: CalibrationState,
        direction: GuideDirection, stepNumber: Int,
        dx: Double, dy: Double, posX: Double, posY: Double,
        distance: Double,
    )

    fun onCalibrationCompleted(calibration: GuideCalibration)

    fun onCalibrationFailed()

    fun onGuideStep(stats: GuideStats)

    fun onNotifyDirectMove(mount: GuidePoint)
}
