package nebulosa.imaging.algorithms

import nebulosa.imaging.Image
import kotlin.math.max
import kotlin.math.min

class Grayscale(
    private val red: Float,
    private val green: Float,
    private val blue: Float,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (source.mono) return source

        val result = Image(source.width, source.height, source.header, true)

        for (i in source.r.indices) {
            val gray = max(0f, min(red * source.r[i] + green * source.g[i] + blue * source.b[i], 1f))
            result.r[i] = gray
        }

        return result
    }

    companion object {

        /**
         * Grayscale image using BT709 algorithm.
         */
        @JvmStatic val BT709 = Grayscale(0.2125f, 0.7154f, 0.0721f)

        /**
         * Grayscale image using R-Y algorithm.
         */
        @JvmStatic val RMY = Grayscale(0.5000f, 0.4190f, 0.0810f)

        /**
         * Grayscale image using Y algorithm.
         */
        @JvmStatic val Y = Grayscale(0.2990f, 0.5870f, 0.1140f)
    }
}
