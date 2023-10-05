package nebulosa.guiding

interface GuideListener {

    fun onStateChanged(state: GuideState)

    fun onGuideStep(
        frame: Int, time: Double,
        raDistance: Double, decDistance: Double,
        raDirection: GuideDirection, decDirection: GuideDirection,
        raDuration: Long, decDuration: Long,
    )

    fun onSettlingStarted()

    fun onSettleDone(hasError: Boolean)
}
