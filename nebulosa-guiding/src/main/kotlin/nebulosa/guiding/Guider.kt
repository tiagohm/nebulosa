package nebulosa.guiding

import nebulosa.imaging.Image

interface Guider : Iterable<GuidePoint> {

    val primaryStar: StarPoint

    val lockPosition: GuidePoint

    var searchRegion: Double

    val image: Image?

    val stats: List<GuideStats>

    fun autoSelect(): Boolean

    fun selectGuideStar(x: Double, y: Double): Boolean

    fun deselectGuideStar()

    fun startLooping()

    fun stopLooping()

    fun startGuiding()

    fun stopGuiding()

    fun reset(fullReset: Boolean)

    fun clearCalibration()

    fun loadCalibration(calibration: Calibration)
}
