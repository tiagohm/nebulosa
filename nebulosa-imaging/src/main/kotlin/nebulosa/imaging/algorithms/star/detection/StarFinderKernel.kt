package nebulosa.imaging.algorithms.star.detection

import nebulosa.imaging.algorithms.ConvolutionKernel
import nebulosa.math.Angle
import kotlin.math.*

class StarFinderKernel(
    fwhm: Double,
    ratio: Double = 1.0,
    theta: Angle = 0.0,
    sigma: Double = 1.5,
    normalize: Boolean = true,
) : ConvolutionKernel {

    private val xsigma = fwhm * GAUSSIAN_FWHM_TO_SIGMA
    private val ysigma = xsigma * ratio

    private val cost = cos(theta)
    private val sint = sin(theta)
    private val xsigma2 = xsigma * xsigma
    private val ysigma2 = ysigma * ysigma

    private val a = (cost * cost / (2.0 * xsigma2)) + (sint * sint / (2.0 * ysigma2))

    // CCW
    private val b = 0.5 * cost * sint * ((1.0 / xsigma2) - (1.0 / ysigma2))
    private val c = (sint * sint / (2.0 * xsigma2)) + (cost * cost / (2.0 * ysigma2))

    // Find the extent of an ellipse with radius = sigma_radius*sigma;
    // solve for the horizontal and vertical tangents of an ellipse
    // defined by g(x,y) = f
    private val f = sigma * sigma / 2.0
    private val denom = (a * c) - b * b

    // nx and ny are always odd
    override val xSize = 2 * (max(2.0, sqrt(c * f / denom))).toInt() + 1
    override val ySize = 2 * (max(2.0, sqrt(a * f / denom))).toInt() + 1

    private val xc = xSize / 2
    private val yc = ySize / 2

    private val circular = DoubleArray(xSize * ySize)
    private val elliptical = DoubleArray(circular.size)
    private val mask = IntArray(circular.size)
    private val npixels: Int
    private val gaussianKernelUnmasked = DoubleArray(circular.size)
    private val gaussianKernel = DoubleArray(circular.size)
    private val data: FloatArray

    init {
        val xx = DoubleArray(circular.size)
        val yy = DoubleArray(xx.size)

        for (i in yy.indices) {
            yy[i] = (i % ySize).toDouble()
        }

        for (i in xx.indices) {
            xx[i] = (i / ySize).toDouble()
        }

        for (i in circular.indices) {
            circular[i] = hypot(xx[i] - xc, yy[i] - yc)
            elliptical[i] = a * (xx[i] - xc).pow(2) + 2.0 * b * (xx[i] - xc) * (yy[i] - yc) + c * (yy[i] - yc).pow(2)
            mask[i] = if (elliptical[i] <= f || circular[i] <= 2.0) 1 else 0
            // NOTE: the central (peak) pixel of Gaussian Kernel has a value of 1.
            gaussianKernelUnmasked[i] = exp(-elliptical[i])
            gaussianKernel[i] = gaussianKernelUnmasked[i] * mask[i]
        }

        npixels = mask.sum()

        data = FloatArray(gaussianKernel.size) { gaussianKernel[it].toFloat() }

        if (normalize) {
            val gaussianKernelSum = gaussianKernel.sum()
            val denom = gaussianKernel.sumOf { it * it } - gaussianKernelSum * gaussianKernelSum / npixels
            // val relerr = 1.0 / sqrt(denom)

            val factor = gaussianKernelSum / npixels

            for (i in data.indices) {
                data[i] = (((data[i] - factor) / denom) * mask[i]).toFloat()
            }
        }
    }

    override fun get(index: Int) = data[index]

    override val divisor
        get() = max(1f, data.sum())

    companion object {

        const val GAUSSIAN_SIGMA_TO_FWHM = 2.3548200450309493 // 2.0 * sqrt(2.0 * ln(2.0))
        const val GAUSSIAN_FWHM_TO_SIGMA = 1.0 / GAUSSIAN_SIGMA_TO_FWHM
    }
}
