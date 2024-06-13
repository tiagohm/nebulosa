package nebulosa.guiding

interface GuiderListener {

    fun onStateChanged(state: GuideState, pixelScale: Double) = Unit

    fun onGuideStepped(guideStar: GuideStar) = Unit

    fun onDithered(dx: Double, dy: Double) = Unit

    fun onSettleStarted() = Unit

    fun onSettleDone(error: String?) = Unit

    fun onMessageReceived(message: String) = Unit
}
