package nebulosa.guiding.internal

import nebulosa.imaging.Image
import nebulosa.math.Angle

interface GuideDevice {

    // Camera.

    val cameraIsConnected: Boolean

    val cameraBinning: Int

    val cameraImage: Image

    val cameraPixelScale: Double

    val cameraExposure: Long

    // Mount.

    val mountIsConnected: Boolean

    val mountIsBusy: Boolean

    val mountRightAscension: Angle

    val mountDeclination: Angle

    val mountRightAscensionGuideRate: Double

    val mountDeclinationGuideRate: Double

    val mountPierSideAtEast: Boolean

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

    fun guideTo(direction: GuideDirection, duration: Int): Boolean

    fun notifyDirectMove(mount: Point) = Unit

    companion object {

        const val DEFAULT_CALIBRATION_DURATION = 750
    }
}
