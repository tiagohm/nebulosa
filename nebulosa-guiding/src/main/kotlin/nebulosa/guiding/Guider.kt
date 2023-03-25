package nebulosa.guiding

interface Guider {

    fun autoSelect(): Boolean

    fun selectGuideStar(x: Double, y: Double): Boolean

    fun deselectGuideStar()
}
