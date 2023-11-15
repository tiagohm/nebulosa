package nebulosa.imaging.algorithms

import kotlin.math.max
import kotlin.math.sqrt

open class MatrixConvolutionKernel(
    private val kernel: FloatArray,
    override val xSize: Int = sqrt(kernel.size.toDouble()).toInt(),
    override val ySize: Int = xSize,
) : ConvolutionKernel {

    override val divisor
        get() = max(1f, kernel.sum())

    override fun get(index: Int) = kernel[index]
}
