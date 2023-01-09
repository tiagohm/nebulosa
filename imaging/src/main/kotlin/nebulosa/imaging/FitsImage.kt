package nebulosa.imaging

import nebulosa.imaging.algorithms.CfaPattern.Companion.cfaPattern
import nebulosa.imaging.algorithms.Debayer
import nebulosa.math.map
import nom.tam.fits.Fits
import java.awt.image.*
import kotlin.math.max
import kotlin.math.min

@Suppress("UNCHECKED_CAST")
class FitsImage(
    val fits: Fits,
    debayer: Boolean = true,
) : Image(
    fits.imageHDU(0)!!.naxis(1),
    fits.imageHDU(0)!!.naxis(2),
    fits.imageHDU(0)!!.let { it.axes.size != 3 && !(debayer && it.axes.size == 2 && it.cfaPattern != null) },
) {

    fun read() {
        val hdu = fits.imageHDU(0)!!
        val axes = hdu.axes
        val pixels = hdu.kernel as Array<*>
        val bitpix = hdu.bitpix

        if (axes.size == 2) {
            val bayer = hdu.cfaPattern

            val (min, max) = when (bitpix.numberType) {
                Byte::class.java -> writeByteArray(ImageChannel.RED, pixels as Array<ByteArray>)
                Short::class.java -> writeShortArray(ImageChannel.RED, pixels as Array<ShortArray>)
                Int::class.java -> writeIntArray(ImageChannel.RED, pixels as Array<IntArray>)
                Float::class.java -> writeFloatArray(ImageChannel.RED, pixels as Array<FloatArray>)
                Double::class.java -> writeDoubleArray(ImageChannel.RED, pixels as Array<DoubleArray>)
                else -> return
            }

            mapData(min, max)

            if (bayer != null) {
                Debayer(bayer).transform(this)
            }
        } else {
            var max = Float.MIN_VALUE
            var min = Float.MAX_VALUE

            for (channel in ImageChannel.RGB) {
                val minMax = when (bitpix.numberType) {
                    Byte::class.java -> writeByteArray(channel, pixels[channel.offset] as Array<ByteArray>)
                    Short::class.java -> writeShortArray(channel, pixels[channel.offset] as Array<ShortArray>)
                    Int::class.java -> writeIntArray(channel, pixels[channel.offset] as Array<IntArray>)
                    Float::class.java -> writeFloatArray(channel, pixels[channel.offset] as Array<FloatArray>)
                    Double::class.java -> writeDoubleArray(channel, pixels[channel.offset] as Array<DoubleArray>)
                    else -> return
                }

                min = min(min, minMax[0])
                max = max(max, minMax[1])
            }

            mapData(min, max)
        }

        fits.close()
    }

    override fun clone(): FitsImage {
        val image = FitsImage(fits, !mono)
        data.copyInto(image.data)
        return image
    }

    private fun mapData(min: Float, max: Float) {
        if (min < 0f || max > 1f) {
            for (i in data.indices) {
                data[i] = map(data[i], min, max, 0f, 1f)
            }
        }
    }
}
