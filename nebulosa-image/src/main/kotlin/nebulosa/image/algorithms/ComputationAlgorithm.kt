package nebulosa.image.algorithms

import nebulosa.image.Image

fun interface ComputationAlgorithm<out T> {

    fun compute(source: Image): T
}
