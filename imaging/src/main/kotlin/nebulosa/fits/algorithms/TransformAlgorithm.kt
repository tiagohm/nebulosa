package nebulosa.fits.algorithms

import nebulosa.fits.Image

fun interface TransformAlgorithm {

    fun transform(source: Image): Image

    companion object {

        @JvmStatic
        fun of(vararg algorithms: TransformAlgorithm) = TransformAlgorithm { algorithms.transform(it) }

        @JvmStatic
        fun of(algorithms: Iterable<TransformAlgorithm>) = TransformAlgorithm { algorithms.transform(it) }

        @JvmStatic
        @JvmName("apply")
        fun Array<out TransformAlgorithm>.transform(source: Image) = fold(source) { s, a -> a.transform(s) }

        @JvmStatic
        @JvmName("apply")
        fun Iterable<TransformAlgorithm>.transform(source: Image) = fold(source) { s, a -> a.transform(s) }
    }
}
