package nebulosa.imaging.algorithms.transformation.convolution

object Emboss : Convolution(
    floatArrayOf(
        -1f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 1f,
    )
)
