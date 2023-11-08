package nebulosa.imaging.algorithms

import kotlin.math.max
import kotlin.math.sqrt

open class MatrixConvolutionKernel(private val kernel: FloatArray) : ConvolutionKernel {

    override val size = sqrt(kernel.size.toFloat()).toInt()

    override val divisor
        get() = max(1f, kernel.sum())

    override fun get(index: Int) = kernel[index]
}
