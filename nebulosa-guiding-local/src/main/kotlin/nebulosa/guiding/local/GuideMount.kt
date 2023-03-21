package nebulosa.guiding.local

interface GuideMount {

    val connected: Boolean

    fun beginCalibration(currentLocation: Point): Boolean

    fun updateCalibrationState(currentLocation: Point): Boolean

    fun notifyGuidingStarted()

    fun notifyGuidingStopped()

    fun notifyGuidingPaused()

    fun notifyGuidingResumed()

    fun notifyGuidingDithered(dx: Double, dy: Double, mountCoords: Boolean)

    fun notifyGuidingDitherSettleDone(success: Boolean)

    fun notifyDirectMove(distance: Point)
}
