package nebulosa.guiding.internal

import nebulosa.math.Angle

interface GuideMount {

    val connected: Boolean

    val busy: Boolean

    val raParity: GuideParity

    val decParity: GuideParity

    val calibrationFlipRequiresDecFlip: Boolean

    val declination: Angle

    val guidingEnabled: Boolean

    val declinationGuideMode: DeclinationGuideMode

    val maxDeclinationDuration: Int

    val maxRightAscensionDuration: Int

    val guidingRAOnly
        get() = declinationGuideMode == DeclinationGuideMode.NONE

    val xGuideAlgorithm: GuideAlgorithm

    val yGuideAlgorithm: GuideAlgorithm

    fun beginCalibration(currentLocation: Point): Boolean

    fun updateCalibrationState(currentLocation: Point): Boolean

    fun notifyGuidingStarted()

    fun notifyGuidingStopped()

    fun notifyGuidingPaused()

    fun notifyGuidingResumed()

    fun notifyGuidingDithered(dx: Double, dy: Double, mountCoords: Boolean)

    fun notifyGuidingDitherSettleDone(success: Boolean)

    fun notifyDirectMove(distance: Point)

    fun moveTo(direction: GuideDirection, duration: Int): Boolean
}
