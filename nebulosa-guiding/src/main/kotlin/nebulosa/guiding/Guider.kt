package nebulosa.guiding

interface Guider {

    val starCount: Int

    fun autoSelect(): Boolean

    fun selectGuideStar(x: Double, y: Double)

    fun deselectGuideStar()
}
