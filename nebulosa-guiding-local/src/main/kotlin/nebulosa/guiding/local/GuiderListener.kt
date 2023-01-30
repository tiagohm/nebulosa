package nebulosa.guiding.local

interface GuiderListener {

    fun onLockPositionChanged(guider: Guider, position: Point)

    fun onStarSelected(guider: Guider, star: Star)

    fun onGuidingDithered(guider: Guider, dx: Float, dy: Float, mountCoordinate: Boolean)
}
