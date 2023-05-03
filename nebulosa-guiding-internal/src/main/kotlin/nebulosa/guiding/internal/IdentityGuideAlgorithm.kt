package nebulosa.guiding.internal

import nebulosa.guiding.GuideAxis

class IdentityGuideAlgorithm(override val axis: GuideAxis) : GuideAlgorithm {

    override var minMove = -1.0

    override fun compute(input: Double) = input

    override fun reset() {}
}
