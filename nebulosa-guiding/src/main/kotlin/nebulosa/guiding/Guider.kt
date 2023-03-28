package nebulosa.guiding

interface Guider : Iterable<GuidePoint> {

    val primaryStar: StarPoint

    val lockPosition: GuidePoint

    var searchRegion: Double

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
