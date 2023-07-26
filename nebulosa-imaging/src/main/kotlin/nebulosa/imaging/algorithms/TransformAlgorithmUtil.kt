package nebulosa.imaging.algorithms

import nebulosa.imaging.Image

@JvmName("apply")
@Suppress("NOTHING_TO_INLINE")
inline fun Array<out TransformAlgorithm>.transform(source: Image) = fold(source) { s, a -> a.transform(s) }

@JvmName("apply")
@Suppress("NOTHING_TO_INLINE")
inline fun Iterable<TransformAlgorithm>.transform(source: Image) = fold(source) { s, a -> a.transform(s) }

@Suppress("NOTHING_TO_INLINE")
inline operator fun TransformAlgorithm.plus(other: TransformAlgorithm) = TransformAlgorithm { transform(it).transform(other) }
