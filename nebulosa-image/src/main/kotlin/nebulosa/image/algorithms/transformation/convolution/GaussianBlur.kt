package nebulosa.image.algorithms.transformation.convolution

import kotlin.math.exp

class GaussianBlur(
    sigma: Double = 1.4,
    size: Int = 5,
) : Convolution(kernel2D(sigma * sigma, size)) {

    init {
        require(sigma <= 5.0) { "sigma <= 5.0: $sigma" }
        require(sigma >= 0.5) { "sigma >= 0.5: $sigma" }
    }

    companion object {

        @JvmStatic
        fun gaussian2D(sigmaSquared: Double, x: Int, y: Int): Double {
            return exp((x * x + y * y) / (-2.0 * sigmaSquared)) / (2.0 * Math.PI * sigmaSquared)
        }

        @JvmStatic
        fun kernel2D(sigmaSquared: Double, size: Int): ConvolutionKernel {
            require(size > 2) { "size > 2: $size" }
            require(size % 2 == 1) { "size must be odd: $size" }

            val r = size / 2

            val kernel = FloatArray(size * size)
            var i = 0

            for (y in -r..r) {
                for (x in -r..r) {
                    kernel[i++] = gaussian2D(sigmaSquared, x, y).toFloat()
                }
            }

            val min = kernel[0]

            for (k in kernel.indices) {
                kernel[k] /= min
            }

            return MatrixConvolutionKernel(kernel)
        }
    }
}
