package nebulosa.fits.algorithms

import nebulosa.fits.FitsImage

class ScreenTransformFunction(
    private val midtone: Float,
    private val shadow: Float = 0f,
    private val highlight: Float = 1f,
) : TransformAlgorithm {

    private val canTransform = midtone != 0.5f || shadow != 0f || highlight != 1f
    private val rangeFactor = if (shadow == highlight) 1f else (1f / (highlight - shadow))
    private val k1 = (midtone - 1f) * rangeFactor
    private val k2 = ((2f * midtone) - 1f) * rangeFactor
    private val lut = if (canTransform) FloatArray(65536) else FloatArray(0)

    override fun transform(image: FitsImage): FitsImage {
        if (!canTransform) return image

        lut.fill(Float.NaN)

        for (i in image.data.indices) {
            val pixel = image.data[i]
            val color = pixel.df(midtone, shadow, highlight, k1, k2)
            image.data[i] = color
        }

        return image
    }

    // https://pixinsight.com/doc/docs/XISF-1.0-spec/XISF-1.0-spec.html#__XISF_Data_Objects_:_XISF_Image_:_Display_Function__
    // https://pixinsight.com/tutorials/24-bit-stf/

    private fun Float.df(midtone: Float, s: Float, h: Float, k1: Float, k2: Float): Float {
        val p = (this * 65535f).toInt()
        if (!lut[p].isNaN()) return lut[p]
        if (this < s) return 0f
        if (this > h) return 1f
        val i = this - s
        val value = (i * k1) / (i * k2 - midtone)
        lut[p] = value
        return value
    }
}
