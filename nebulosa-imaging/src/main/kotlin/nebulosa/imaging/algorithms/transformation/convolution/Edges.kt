package nebulosa.imaging.algorithms.transformation.convolution

object Edges : Convolution(
    floatArrayOf(
        0f, -1f, 0f,
        -1f, 4f, -1f,
        0f, -1f, 0f,
    )
)
