package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min

abstract class Convolution(
    private val kernel: Array<FloatArray>,
    private val divisor: Float,
) : TransformAlgorithm {

    @JvmField val size = kernel.size

    init {
        require(kernel.size in 3..99) { "kernel size in [3..99]: ${kernel.size}" }
        require(kernel.size % 2 == 1) { "kernel size must be odd: ${kernel.size}" }
    }

    constructor(kernel: Array<FloatArray>) : this(kernel, max(1f, kernel.fold(0f) { s, e -> s + e.sum() }))

    override fun transform(source: Image): Image {
        val radius = size / 2
        val c = FloatArray(source.numberOfChannels)
        val kernelSize = size * size

        // TODO: Otimizar isso!
        val cached = Array(3) { FloatArray(source.width * source.height) }

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                var div = 0f
                var processedKernelSize = 0

                c.fill(0f)

                for (i in kernel.indices) {
                    val ir = i - radius
                    var t = y + ir

                    if (t < 0) continue
                    if (t >= source.height) break

                    for (j in kernel.indices) {
                        val jr = j - radius
                        t = x + jr

                        if (t < 0) continue

                        if (t < source.width) {
                            val k = kernel[i][j]

                            div += k
                            val index = (y + ir) * source.stride + (x + jr)
                            for (p in 0 until source.numberOfChannels) c[p] += k * source.data[p][index]

                            processedKernelSize++
                        }
                    }
                }

                if (processedKernelSize == kernelSize) {
                    div = divisor
                }

                for (p in 0 until source.numberOfChannels) c[p] /= div

                val index = y * source.width + x
                for (p in 0 until source.numberOfChannels) cached[p][index] = max(0f, min(c[p], 1f))
            }
        }

        for (i in cached.indices) {
            cached[i].copyInto(source.data[i])
        }

        return source
    }
}
