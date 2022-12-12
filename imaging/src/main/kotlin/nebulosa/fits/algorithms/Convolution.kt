package nebulosa.fits.algorithms

import nebulosa.fits.Image
import kotlin.math.max
import kotlin.math.min

abstract class Convolution(
    private val kernel: Array<FloatArray>,
    val divisor: Float,
) : TransformAlgorithm {

    @JvmField val size = kernel.size

    private val cached = Array(3) { FloatArray(0) }

    init {
        require(kernel.size in 3..99) { "kernel size in [3..99]: ${kernel.size}" }
        require(kernel.size % 2 == 1) { "kernel size must be odd: ${kernel.size}" }
    }

    constructor(kernel: Array<FloatArray>) : this(kernel, max(1f, kernel.fold(0f) { s, e -> s + e.sum() }))

    override fun transform(source: Image): Image {
        val radius = size / 2
        val c = FloatArray(source.pixelStride)
        val kernelSize = size * size

        for (i in c.indices) cached[i] = FloatArray(source.width * source.height)

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
                            val index = (y + ir) * source.stride + (x + jr) * source.pixelStride
                            for (p in 0 until source.pixelStride) c[p] += k * source.data[index + p]

                            processedKernelSize++
                        }
                    }
                }

                if (processedKernelSize == kernelSize) {
                    div = divisor
                }

                for (p in 0 until source.pixelStride) c[p] /= div

                val index = y * source.width + x
                for (p in 0 until source.pixelStride) cached[p][index] = max(0f, min(c[p], 1f))
            }
        }

        for (i in cached.indices) {
            for (k in cached[i].indices) source.data[k * source.pixelStride + i] = cached[i][k]
        }

        return source
    }
}
