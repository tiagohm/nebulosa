package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

fun interface ComputationAlgorithm<out T> {

    fun compute(source: Image): T
}
