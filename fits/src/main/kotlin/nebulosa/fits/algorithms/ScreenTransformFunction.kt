package nebulosa.fits.algorithms

import nebulosa.fits.FitsImage

class ScreenTransformFunction(
    private val midtone: Float,
    private val shadow: Float = 0f,
    private val highlight: Float = 1f,
) {

    private val rangeFactor = if (shadow == highlight) 1f else (1f / (highlight - shadow))
    private val k1 = (midtone - 1f) * rangeFactor
    private val k2 = ((2f * midtone) - 1f) * rangeFactor
    private val lut = FloatArray(256)

    fun transform(image: FitsImage): FitsImage {
        lut.fill(Float.NaN)

        for (i in image.data.indices) {
            val pixel = image.data[i]
            val red = ((pixel ushr 16) and 0xff).df(midtone, shadow, highlight, k1, k2)
            val green = ((pixel ushr 8) and 0xff).df(midtone, shadow, highlight, k1, k2)
            val blue = (pixel and 0xff).df(midtone, shadow, highlight, k1, k2)
            image.data[i] = (red shl 16) or (green shl 8) or blue
        }

        return image
    }

    // https://pixinsight.com/doc/docs/XISF-1.0-spec/XISF-1.0-spec.html#__XISF_Data_Objects_:_XISF_Image_:_Display_Function__
    // https://pixinsight.com/tutorials/24-bit-stf/

    private fun Int.df(midtone: Float, s: Float, h: Float, k1: Float, k2: Float): Int {
        if (!lut[this].isNaN()) return (lut[this] * 255f).toInt()
        val p = this / 255f
        if (p < s) return 0
        if (p > h) return 255
        val i = p - s
        val value = (i * k1) / (i * k2 - midtone)
        lut[this] = value
        return (value * 255f).toInt()
    }
}
