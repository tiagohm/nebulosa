package nebulosa.image.algorithms.transformation

import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeyword
import nebulosa.image.Image
import nebulosa.image.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min

data class Grayscale(
    private val red: Float,
    private val green: Float,
    private val blue: Float,
) : TransformAlgorithm {

    override fun transform(source: Image): Image {
        if (source.mono) return source

        val newHeader = FitsHeader(source.header)
        newHeader.add(FitsKeyword.NAXIS, 2)
        newHeader.delete(FitsKeyword.NAXIS3)

        val result = Image(source.width, source.height, newHeader, true)

        for (i in source.red.indices) {
            result.red[i] = max(0f, min(red * source.red[i] + green * source.green[i] + blue * source.blue[i], 1f))
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
