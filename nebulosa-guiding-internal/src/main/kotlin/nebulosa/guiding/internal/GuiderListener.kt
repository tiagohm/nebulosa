package nebulosa.guiding.internal

interface GuiderListener {

    fun onLockPositionChanged(guider: MultiStarGuider, position: Point)

    fun onStarSelected(guider: MultiStarGuider, star: Star)

    fun onGuidingDithered(guider: MultiStarGuider, dx: Double, dy: Double, mountCoordinate: Boolean)

    fun onCalibrationFailed()

    fun onGuidingStopped()

    fun onLockShiftLimitReached()

    fun onLooping(frameNumber: Int, start: Star?)

    fun onStarLost()

    fun onLockPositionLost()

    fun onStartCalibration()

    fun onCalibrationStep(
        calibrationState: CalibrationState,
        direction: GuideDirection, stepNumber: Int,
        dx: Double, dy: Double, posX: Double, posY: Double,
        distance: Double,
    )

    fun onCalibrationCompleted()
}
