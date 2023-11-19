package nebulosa.imaging.algorithms

object Emboss : Convolution(
    floatArrayOf(
        -1f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 1f,
    )
)
