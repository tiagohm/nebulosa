package nebulosa.image.algorithms.transformation.convolution

interface ConvolutionKernel {

    val width: Int

    val height: Int

    operator fun get(x: Int, y: Int): Float

    val divisor: Float
}
