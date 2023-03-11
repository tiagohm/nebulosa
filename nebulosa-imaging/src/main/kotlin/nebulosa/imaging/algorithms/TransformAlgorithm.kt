package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

fun interface TransformAlgorithm {

    fun transform(source: Image): Image

    companion object {

        @JvmStatic
        fun of(vararg algorithms: TransformAlgorithm) = TransformAlgorithm { algorithms.transform(it) }

        @JvmStatic
        fun of(algorithms: Iterable<TransformAlgorithm>) = TransformAlgorithm { algorithms.transform(it) }

        @JvmName("apply")
        @Suppress("NOTHING_TO_INLINE")
        inline fun Array<out TransformAlgorithm>.transform(source: Image) = fold(source) { s, a -> a.transform(s) }

        @JvmName("apply")
        @Suppress("NOTHING_TO_INLINE")
        inline fun Iterable<TransformAlgorithm>.transform(source: Image) = fold(source) { s, a -> a.transform(s) }
    }
}
