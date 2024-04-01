package nebulosa.image.algorithms.transformation.convolution

data object Edges : Convolution(
    floatArrayOf(
        0f, -1f, 0f,
        -1f, 4f, -1f,
        0f, -1f, 0f,
    )
)
