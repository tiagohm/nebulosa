package nebulosa.guiding

interface GuiderListener {

    fun onStateChanged(state: GuideState, pixelScale: Double)

    fun onGuideStepped(guideStar: GuideStar)

    fun onDithered(dx: Double, dy: Double)

    fun onMessageReceived(message: String)
}
