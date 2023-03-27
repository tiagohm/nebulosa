package nebulosa.guiding.internal

interface GuiderListener {

    fun onLockPositionChanged(position: Point)

    fun onStarSelected(star: Star)

    fun onGuidingDithered(dx: Double, dy: Double, mountCoordinate: Boolean)

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
