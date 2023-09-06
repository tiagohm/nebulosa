package nebulosa.guiding.internal

interface GuidingPulse {

    fun guideNorth(duration: Int): Boolean

    fun guideSouth(duration: Int): Boolean

    fun guideWest(duration: Int): Boolean

    fun guideEast(duration: Int): Boolean
}
