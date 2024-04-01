package nebulosa.image.algorithms

import nebulosa.image.Image

fun interface TransformAlgorithm {

    fun transform(source: Image): Image

    companion object {

        @JvmStatic
        fun of(vararg algorithms: TransformAlgorithm) = TransformAlgorithm { algorithms.transform(it) }

        @JvmStatic
        fun of(algorithms: Iterable<TransformAlgorithm>) = TransformAlgorithm { algorithms.transform(it) }
    }
}
