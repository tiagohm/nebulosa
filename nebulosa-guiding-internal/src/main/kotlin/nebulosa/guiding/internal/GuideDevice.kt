package nebulosa.guiding.internal

import nebulosa.guiding.GuideParity
import nebulosa.imaging.Image
import nebulosa.math.Angle

interface GuideDevice {

    // Camera.

    val cameraBinning: Int

    val cameraImage: Image

    val cameraPixelScale: Double

    val cameraExposure: Long

    // Mount.

    val mountRightAscension: Angle

    val mountDeclination: Angle

    val mountRightAscensionGuideRate: Double

    val mountDeclinationGuideRate: Double

    val mountPierSideAtEast: Boolean

    fun awaitIfMountIsBusy()

    // Rotator.

    val rotatorAngle: Angle

    // Guiding.

    val rightAscensionParity: GuideParity

    val declinationParity: GuideParity

    val calibrationFlipRequiresDecFlip: Boolean

    val calibrationDuration: Int

    val calibrationDistance: Int

    val declinationCompensationEnabled: Boolean

    val guidingEnabled: Boolean

    val declinationGuideMode: DeclinationGuideMode

    val maxDeclinationDuration: Int

    val maxRightAscensionDuration: Int

    val guidingRAOnly
        get() = declinationGuideMode == DeclinationGuideMode.NONE

    val xGuideAlgorithm: GuideAlgorithm

    val yGuideAlgorithm: GuideAlgorithm

    fun capture(duration: Long)

    fun guideNorth(duration: Int): Boolean

    fun guideSouth(duration: Int): Boolean

    fun guideWest(duration: Int): Boolean

    fun guideEast(duration: Int): Boolean

    fun notifyDirectMove(mount: Point) = Unit

    companion object {

        const val DEFAULT_CALIBRATION_DURATION = 750
    }
}
