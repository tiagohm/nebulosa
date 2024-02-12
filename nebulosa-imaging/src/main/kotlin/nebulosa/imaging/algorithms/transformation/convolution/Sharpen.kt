package nebulosa.imaging.algorithms.transformation.convolution

data object Sharpen : Convolution(
    floatArrayOf(
        0f, -1f, 0f,
        -1f, 5f, -1f,
        0f, -1f, 0f,
    )
)
