package nebulosa.imaging.algorithms

import kotlin.math.exp

class GaussianBlur(
    private val sigma: Double = 1.4,
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
        private fun kernel2D(sigmaSquared: Double, size: Int): Array<FloatArray> {
            require(size > 2) { "size > 2: $size" }
            require(size % 2 == 1) { "size must be odd: $size" }

            val r = size / 2

            val kernel = Array(size) { FloatArray(size) }

            for (y in -r..r) {
                for (x in -r..r) {
                    kernel[y + r][x + r] = gaussian2D(sigmaSquared, x, y).toFloat()
                }
            }

            val min = kernel[0][0]

            for (i in kernel.indices) {
                for (j in kernel.indices) {
                    kernel[i][j] /= min
                }
            }

            return kernel
        }
    }
}
