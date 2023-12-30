package nebulosa.imaging.algorithms.transformation

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.TransformAlgorithm
import kotlin.math.max
import kotlin.math.min

/**
 * @see <a href="https://pixinsight.com/doc/docs/XISF-1.0-spec/XISF-1.0-spec.html#__XISF_Data_Objects_:_XISF_Image_:_Display_Function__">Adaptive Display Function Algorithm</a>
 * @see <a href="https://pixinsight.com/tutorials/24-bit-stf/">24-Bit Screen LUTs</a>
 */
data class ScreenTransformFunction(
    private val midtone: Float,
    private val shadow: Float = 0f,
    private val highlight: Float = 1f,
) : TransformAlgorithm {

    data class Parameters(
        val midtone: Float = 0.5f,
        val shadow: Float = 0f,
        val highlight: Float = 1f,
    ) {

        companion object {

            @JvmStatic val EMPTY = Parameters()
        }
    }

    private val canTransform = midtone != 0.5f || shadow != 0f || highlight != 1f
    private val rangeFactor = if (shadow == highlight) 1f else (1f / (highlight - shadow))
    private val k1 = (midtone - 1f) * rangeFactor
    private val k2 = (2f * midtone - 1f) * rangeFactor
    private val lut = if (canTransform) FloatArray(65536) else FloatArray(0)

    constructor(parameters: Parameters) : this(parameters.midtone, parameters.shadow, parameters.highlight)

    override fun transform(source: Image): Image {
        if (!canTransform) return source

        lut.fill(Float.NaN)

        for (i in source.r.indices) {
            source.r[i] = source.r[i].df(midtone, shadow, highlight, k1, k2)

            if (!source.mono) {
                source.g[i] = source.g[i].df(midtone, shadow, highlight, k1, k2)
                source.b[i] = source.b[i].df(midtone, shadow, highlight, k1, k2)
            }
        }

        return source
    }

    private fun Float.df(midtone: Float, s: Float, h: Float, k1: Float, k2: Float): Float {
        val p = max(0, min((this * 65535f).toInt(), 65535))
        if (!lut[p].isNaN()) return lut[p]
        if (this < s) return 0f
        if (this > h) return 1f
        val i = this - s
        val value = (i * k1) / (i * k2 - midtone)
        lut[p] = value
        return value
    }
}
