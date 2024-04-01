package nebulosa.image.algorithms.transformation.convolution

data object Emboss : Convolution(
    floatArrayOf(
        -1f, 0f, 0f,
        0f, 0f, 0f,
        0f, 0f, 1f,
    )
)
