package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min

open class Convolution(
    private val kernel: ConvolutionKernel,
    private val dynamicDivisorForEdges: Boolean = true,
    private val normalize: Boolean = true,
) : TransformAlgorithm {

    constructor(
        kernel: FloatArray,
        dynamicDivisorForEdges: Boolean = true,
        normalize: Boolean = true,
    ) : this(MatrixConvolutionKernel(kernel), dynamicDivisorForEdges, normalize)

    init {
        require(kernel.width in 3..99) { "kernel size bust be in range [3..99]: ${kernel.width}" }
        require(kernel.width % 2 == 1) { "kernel size must be odd: ${kernel.width}" }
        require(kernel.height in 3..99) { "kernel size bust be in range [3..99]: ${kernel.height}" }
        require(kernel.height % 2 == 1) { "kernel size must be odd: ${kernel.height}" }
    }

    override fun transform(source: Image): Image {
        val xRadius = kernel.width / 2
        val yRadius = kernel.height / 2

        val c = FloatArray(source.numberOfChannels)
        val cache = Array(source.numberOfChannels) { Array(kernel.height) { FloatArray(source.width) } }

        val kernelDivisor = kernel.divisor

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                var processedDivisor = 0f
                var processedKernelSize = 0

                c.fill(0f)

                for (i in 0 until kernel.height) {
                    val ir = i - yRadius
                    val a = y + ir

                    if (a < 0) continue
                    if (a >= source.height) break

                    for (j in 0 until kernel.width) {
                        val jr = j - xRadius
                        val b = x + jr

                        if (b >= 0 && b < source.width) {
                            val k = kernel[j, i]

                            processedDivisor += k
                            val index = a * source.stride + b
                            for (p in c.indices) c[p] += k * source.data[p][index]

                            processedKernelSize++
                        }
                    }
                }

                var offset = 0f
                var divisor = if (dynamicDivisorForEdges) processedDivisor else kernelDivisor

                if (normalize) {
                    if (divisor < 0f) {
                        divisor = -divisor
                        offset = 1f
                    }
                }

                if (divisor == 0f) {
                    divisor = 1f
                    offset = 0.5f
                }

                for (p in c.indices) c[p] /= divisor
                for (p in c.indices) c[p] += offset

                val cacheIdx = y % kernel.height
                for (p in c.indices) cache[p][cacheIdx][x] = max(0f, min(c[p], 1f))
            }

            val r = y - yRadius

            if (r >= 0) {
                val index = r * source.width
                val k = r % kernel.height

                for (p in c.indices) {
                    cache[p][k].copyInto(source.data[p], index)
                }
            }
        }

        repeat(yRadius) {
            val r = source.height - it - 1
            val index = r * source.width
            val k = r % kernel.height

            for (p in c.indices) {
                cache[p][k].copyInto(source.data[p], index)
            }
        }

        return source
    }
}
