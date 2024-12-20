package nebulosa.guiding.internal

interface GuideAlgorithm {

    val axis: GuideAxis

    var minMove: Double

    fun compute(input: Double): Double

    /**
     * Produces a mount move when the guide star
     * has been lost (dead reckoning).
     */
    fun deduce() = 0.0

    fun reset()
}
