package nebulosa.imaging.algorithms

object Edges : Convolution(
    arrayOf(
        floatArrayOf(0f, -1f, 0f),
        floatArrayOf(-1f, 4f, -1f),
        floatArrayOf(0f, -1f, 0f),
    )
)
