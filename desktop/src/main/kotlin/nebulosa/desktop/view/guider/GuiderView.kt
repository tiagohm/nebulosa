package nebulosa.desktop.view.guider

import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.view.View
import nebulosa.guiding.GuideStats
import nebulosa.guiding.Guider
import nebulosa.guiding.NoiseReductionMethod
import nebulosa.guiding.internal.DeclinationGuideMode
import nebulosa.imaging.Image

interface GuiderView : View, ImageViewer.MouseListener {

    val algorithmRA: GuideAlgorithmType

    val maxDurationRA: Int

    val hysteresisRA: Double

    val aggressivenessRA: Double

    val minimumMoveRA: Double

    val slopeWeightRA: Double

    val fastSwitchForLargeDeflectionsRA: Boolean

    val algorithmDEC: GuideAlgorithmType

    val maxDurationDEC: Int

    val guideModeDEC: DeclinationGuideMode

    val hysteresisDEC: Double

    val aggressivenessDEC: Double

    val minimumMoveDEC: Double

    val slopeWeightDEC: Double

    val fastSwitchForLargeDeflectionsDEC: Boolean

    val calibrationStep: Int

    val useDECCompensation: Boolean

    val assumeDECOrthogonalToRA: Boolean

    val ditherMode: DitherMode

    val ditherAmount: Double

    val ditherRAOnly: Boolean

    val exposureTime: Long

    val exposureDelay: Long

    val searchRegion: Double

    val noiseReductionMethod: NoiseReductionMethod

    fun updateStatus(text: String)

    fun updateStarProfile(guider: Guider, image: Image = guider.image!!)

    fun updateGraph(stats: List<GuideStats>, maxRADuration: Double, maxDECDuration: Double)

    fun updateGraphInfo(rmsRA: Double, rmsDEC: Double, rmsTotal: Double, pixelScale: Double)
}
