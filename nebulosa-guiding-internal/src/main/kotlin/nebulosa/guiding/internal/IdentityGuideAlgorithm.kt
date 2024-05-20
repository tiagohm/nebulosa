package nebulosa.guiding.internal

class IdentityGuideAlgorithm(override val axis: GuideAxis) : GuideAlgorithm {

    override var minMove = -1.0

    override fun compute(input: Double) = input

    override fun reset() = Unit
}
