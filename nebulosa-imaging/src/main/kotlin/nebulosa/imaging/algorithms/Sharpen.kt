package nebulosa.imaging.algorithms

object Sharpen : Convolution(
    arrayOf(
        floatArrayOf(0f, -1f, 0f),
        floatArrayOf(-1f, 5f, -1f),
        floatArrayOf(0f, -1f, 0f),
    )
)
