package nebulosa.guiding

import nebulosa.imaging.Image

interface Guider : Iterable<GuidePoint> {

    val primaryStar: StarPoint

    val lockPosition: GuidePoint

    val searchRegion: Double

    val image: Image?

    val stats: List<GuideStats>

    fun autoSelect(): Boolean

    fun selectGuideStar(x: Double, y: Double): Boolean

    fun deselectGuideStar()

    val isLooping: Boolean

    fun startLooping()

    fun stopLooping()

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
}
