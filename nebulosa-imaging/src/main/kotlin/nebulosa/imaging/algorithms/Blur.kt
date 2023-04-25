package nebulosa.imaging.algorithms

object Blur : Convolution(
    arrayOf(
        floatArrayOf(1f, 2f, 3f, 2f, 1f),
        floatArrayOf(2f, 4f, 5f, 4f, 2f),
        floatArrayOf(3f, 5f, 6f, 5f, 3f),
        floatArrayOf(2f, 4f, 5f, 4f, 2f),
        floatArrayOf(1f, 2f, 3f, 2f, 1f),
    )
)
