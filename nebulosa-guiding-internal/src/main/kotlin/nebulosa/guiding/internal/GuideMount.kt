package nebulosa.guiding.internal

import nebulosa.math.Angle

interface GuideMount {

    val connected: Boolean

    val busy: Boolean

    val raParity: GuideParity

    val decParity: GuideParity

    val calibrationFlipRequiresDecFlip: Boolean

    val calibrationDuration: Int

    val declination: Angle

    val guidingEnabled: Boolean

    val declinationGuideMode: DeclinationGuideMode

    val maxDeclinationDuration: Int

    val maxRightAscensionDuration: Int

    val guidingRAOnly
        get() = declinationGuideMode == DeclinationGuideMode.NONE

    val xGuideAlgorithm: GuideAlgorithm

    val yGuideAlgorithm: GuideAlgorithm

    fun notifyGuidingStarted()

    fun notifyGuidingStopped()

    fun notifyGuidingPaused()

    fun notifyGuidingResumed()

    fun notifyGuidingDithered(dx: Double, dy: Double, mountCoords: Boolean)

    fun notifyGuidingDitherSettleDone(success: Boolean)

    fun notifyDirectMove(distance: Point)

    fun guideTo(direction: GuideDirection, duration: Int): Boolean

    companion object {

        const val DEFAULT_CALIBRATION_DURATION = 750
    }
}
