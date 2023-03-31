package nebulosa.guiding.internal

import nebulosa.guiding.Dither
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

    // Rotator.

    val rotatorAngle: Angle

    // Guiding.

    val dither: Dither

    val ditherAmount: Double

    val ditherRAOnly: Boolean

    val calibrationFlipRequiresDecFlip: Boolean

    val assumeDECOrthogonalToRA: Boolean

    val calibrationStep: Int

    val calibrationDistance: Int

    val useDECCompensation: Boolean

    val guidingEnabled: Boolean

    val declinationGuideMode: DeclinationGuideMode

    val maxDECDuration: Int

    val maxRADuration: Int

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
}
