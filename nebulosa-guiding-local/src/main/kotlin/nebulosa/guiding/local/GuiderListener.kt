package nebulosa.guiding.local

interface GuiderListener {

    fun onLockPositionChanged(guider: Guider, position: Point)

    fun onStarSelected(guider: Guider, star: Star)

    fun onGuidingDithered(guider: Guider, dx: Double, dy: Double, mountCoordinate: Boolean)

    fun onCalibrationFailed()

    fun onGuidingStopped()

    fun onLockShiftLimitReached()

    fun onLooping(frameNumber: Int, start: Star?)

    fun onStarLost()
}
