package nebulosa.guiding

interface Guider : Iterable<GuidePoint> {

    val primaryStar: GuidePoint

    val lockPosition: GuidePoint

    var searchRegion: Double

    fun autoSelect(): Boolean

    fun selectGuideStar(x: Double, y: Double): Boolean

    fun deselectGuideStar()

    fun startLooping()

    fun stopLooping()

    fun startGuiding()

    fun stopGuiding()
}
