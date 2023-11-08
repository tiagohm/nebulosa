package nebulosa.imaging.algorithms

interface ConvolutionKernel {

    val size: Int

    operator fun get(index: Int): Float

    val divisor: Float
}
