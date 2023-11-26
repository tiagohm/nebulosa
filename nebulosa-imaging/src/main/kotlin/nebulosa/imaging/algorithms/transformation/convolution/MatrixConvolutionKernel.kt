package nebulosa.imaging.algorithms.transformation.convolution

import kotlin.math.sqrt

open class MatrixConvolutionKernel(
    private val kernel: FloatArray,
    override val width: Int = sqrt(kernel.size.toDouble()).toInt(),
    override val height: Int = width,
) : ConvolutionKernel {

    override val divisor = kernel.sum()

    override fun get(x: Int, y: Int) = kernel[y * width + x]
}
