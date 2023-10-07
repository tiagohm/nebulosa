package nebulosa.guiding

interface GuiderListener {

    fun onStateChange(state: GuideState)

    fun onGuideStep(guideStar: GuideStar)

    fun onMessage(message: String)
}
