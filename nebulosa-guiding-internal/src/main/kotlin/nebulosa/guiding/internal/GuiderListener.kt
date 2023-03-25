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
}
