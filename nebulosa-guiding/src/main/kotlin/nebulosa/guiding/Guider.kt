package nebulosa.guiding

import nebulosa.imaging.Image

interface Guider : Iterable<GuidePoint> {

    val primaryStar: StarPoint

    val lockPosition: GuidePoint

    var searchRegion: Double

    var dither: Dither

    var ditherAmount: Double

    var ditherRAOnly: Boolean

    var calibrationFlipRequiresDecFlip: Boolean

    var assumeDECOrthogonalToRA: Boolean

    var calibrationStep: Int

    var calibrationDistance: Int

    var useDECCompensation: Boolean

    var declinationGuideMode: DeclinationGuideMode

    var maxDECDuration: Int

    var maxRADuration: Int

    val isGuidingRAOnly
        get() = declinationGuideMode == DeclinationGuideMode.NONE

    var noiseReductionMethod: NoiseReductionMethod

    var isGuidingEnabled: Boolean

    fun processImage(image: Image)

    val stats: List<GuideStats>

    fun autoSelect(): Boolean

    fun selectGuideStar(x: Double, y: Double): Boolean

    fun deselectGuideStar()

    val isGuiding: Boolean

    fun startGuiding()

    fun stopGuiding()

    fun reset(fullReset: Boolean)

    val isCalibrating: Boolean

    fun clearCalibration()

    fun loadCalibration(calibration: GuideCalibration)

    fun dither()

    fun registerListener(listener: GuiderListener)

    fun unregisterListener(listener: GuiderListener)

    var isMultiStar: Boolean

    val isLockPositionShiftEnabled: Boolean

    val isSettling: Boolean
}
