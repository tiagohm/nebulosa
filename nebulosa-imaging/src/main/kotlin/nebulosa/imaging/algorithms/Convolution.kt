package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min

open class Convolution(
    private val kernel: ConvolutionKernel,
    private val dynamicDivisorForEdges: Boolean = true,
) : TransformAlgorithm {

    constructor(
        kernel: FloatArray,
        dynamicDivisorForEdges: Boolean = true
    ) : this(MatrixConvolutionKernel(kernel), dynamicDivisorForEdges)

    init {
        require(kernel.xSize in 3..99) { "kernel size bust be in range [3..99]: ${kernel.xSize}" }
        require(kernel.xSize % 2 == 1) { "kernel size must be odd: ${kernel.xSize}" }
        require(kernel.ySize in 3..99) { "kernel size bust be in range [3..99]: ${kernel.ySize}" }
        require(kernel.ySize % 2 == 1) { "kernel size must be odd: ${kernel.ySize}" }
    }

    override fun transform(source: Image): Image {
        val xRadius = kernel.xSize / 2
        val yRadius = kernel.ySize / 2

        val c = FloatArray(source.numberOfChannels)
        val cache = Array(source.numberOfChannels) { Array(kernel.ySize) { FloatArray(source.width) } }

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                var div = 0f
                var processedKernelSize = 0

                c.fill(0f)

                for (i in 0 until kernel.ySize) {
                    val ir = i - yRadius
                    val a = y + ir

                    if (a < 0) continue
                    if (a >= source.height) break

                    for (j in 0 until kernel.xSize) {
                        val jr = j - xRadius
                        val b = x + jr

                        if (b >= 0 && b < source.width) {
                            val k = kernel[j, i]

                            div += k
                            val index = a * source.stride + b
                            for (p in c.indices) c[p] += k * source.data[p][index]

                            processedKernelSize++
                        }
                    }
                }

                if (processedKernelSize == kernel.xSize * kernel.ySize) {
                    // All kernel elements are processed - we are not on the edge.
                    div = kernel.divisor
                } else if (!dynamicDivisorForEdges) {
                    // We are on edge. do we need to use dynamic divisor or not?
                    div = kernel.divisor
                }

                for (p in c.indices) c[p] /= div

                val cacheIdx = y % kernel.ySize
                for (p in c.indices) cache[p][cacheIdx][x] = max(0f, min(c[p], 1f))
            }

            val r = y - yRadius

            if (r >= 0) {
                val index = r * source.width
                val k = r % kernel.ySize

                for (p in c.indices) {
                    cache[p][k].copyInto(source.data[p], index)
                }
            }
        }

        repeat(yRadius) {
            val r = source.height - it - 1
            val index = r * source.width
            val k = r % kernel.ySize

            for (p in c.indices) {
                cache[p][k].copyInto(source.data[p], index)
            }
        }

        return source
    }
}
