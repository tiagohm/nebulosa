package nebulosa.fits

import nebulosa.fits.algorithms.CfaPattern.Companion.cfaPattern
import nebulosa.fits.algorithms.Debayer
import nom.tam.fits.Fits
import java.awt.image.*

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

            if (bayer == null) {
                when (bitpix.numberType) {
                    Byte::class.java -> writeByteArray(ImageChannel.GRAY, pixels as Array<ByteArray>)
                    Short::class.java -> writeShortArray(ImageChannel.GRAY, pixels as Array<ShortArray>)
                    Int::class.java -> writeIntArray(ImageChannel.GRAY, pixels as Array<IntArray>)
                    Float::class.java -> writeFloatArray(ImageChannel.GRAY, pixels as Array<FloatArray>)
                    Double::class.java -> writeDoubleArray(ImageChannel.GRAY, pixels as Array<DoubleArray>)
                }
            } else {
                when (bitpix.numberType) {
                    Byte::class.java -> writeByteArray(ImageChannel.RED, pixels as Array<ByteArray>)
                    Short::class.java -> writeShortArray(ImageChannel.RED, pixels as Array<ShortArray>)
                    Int::class.java -> writeIntArray(ImageChannel.RED, pixels as Array<IntArray>)
                    Float::class.java -> writeFloatArray(ImageChannel.RED, pixels as Array<FloatArray>)
                    Double::class.java -> writeDoubleArray(ImageChannel.RED, pixels as Array<DoubleArray>)
                }

                Debayer(bayer).transform(this)
            }
        } else {
            for (channel in arrayOf(ImageChannel.RED, ImageChannel.GREEN, ImageChannel.BLUE)) {
                when (bitpix.numberType) {
                    Byte::class.java -> writeByteArray(channel, pixels[channel.ordinal] as Array<ByteArray>)
                    Short::class.java -> writeShortArray(channel, pixels[channel.ordinal] as Array<ShortArray>)
                    Int::class.java -> writeIntArray(channel, pixels[channel.ordinal] as Array<IntArray>)
                    Float::class.java -> writeFloatArray(channel, pixels[channel.ordinal] as Array<FloatArray>)
                    Double::class.java -> writeDoubleArray(channel, pixels[channel.ordinal] as Array<DoubleArray>)
                }
            }
        }
    }

    override fun clone(): FitsImage {
        val image = FitsImage(fits, !mono)
        data.copyInto(image.data)
        return image
    }
}
