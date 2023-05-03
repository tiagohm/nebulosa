package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min

abstract class Convolution(
    private val kernel: Array<FloatArray>,
    private val divisor: Float,
    private val dynamicDivisorForEdges: Boolean = true,
) : TransformAlgorithm {

    val size = kernel.size

    init {
        require(kernel.size in 3..99) { "kernel size bust be in range [3..99]: ${kernel.size}" }
        require(kernel.size % 2 == 1) { "kernel size must be odd: ${kernel.size}" }
    }

    constructor(kernel: Array<FloatArray>) : this(kernel, max(1f, kernel.fold(0f) { s, e -> s + e.sum() }))

    override fun transform(source: Image): Image {
        val radius = size / 2
        val c = FloatArray(source.numberOfChannels)
        val kernelSize = size * size

        val cache = Array(source.numberOfChannels) { Array(size) { FloatArray(source.width) } }

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                var div = 0f
                var processedKernelSize = 0

                c.fill(0f)

                for (i in kernel.indices) {
                    val ir = i - radius
                    val a = y + ir

                    if (a < 0) continue
                    if (a >= source.height) break

                    for (j in kernel.indices) {
                        val jr = j - radius
                        val b = x + jr

                        if (b < 0) continue

                        if (b < source.width) {
                            val k = kernel[i][j]

                            div += k
                            val index = a * source.stride + b
                            for (p in c.indices) c[p] += k * source.data[p][index]

                            processedKernelSize++
                        }
                    }
                }

                if (processedKernelSize == kernelSize) {
                    // All kernel elements are processed - we are not on the edge.
                    div = divisor
                } else if (!dynamicDivisorForEdges) {
                    // We are on edge. do we need to use dynamic divisor or not?
                    div = divisor
                }

                for (p in c.indices) c[p] /= div

                val cacheIdx = y % size
                for (p in c.indices) cache[p][cacheIdx][x] = max(0f, min(c[p], 1f))
            }

            val r = y - radius

            if (r >= 0) {
                val index = r * source.width
                val k = r % size

                for (p in c.indices) {
                    cache[p][k].copyInto(source.data[p], index)
                }
            }
        }

        repeat(radius) {
            val r = source.height - it - 1
            val index = r * source.width
            val k = r % size

            for (p in c.indices) {
                cache[p][k].copyInto(source.data[p], index)
            }
        }

        return source
    }
}
