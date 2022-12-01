package nebulosa.fits

import nom.tam.fits.Fits
import nom.tam.fits.header.extra.MaxImDLExt
import java.awt.color.ColorSpace
import java.awt.image.*

@Suppress("UNCHECKED_CAST")
open class FitsImage private constructor(
    val fits: Fits,
    bayerPattern: CfaPattern? = null,
    initialize: Boolean = true,
) : BufferedImage(fits.createColorModel(), fits.createRaster(), false, null) {

    val data: FloatArray = (raster.dataBuffer as FitsDataBuffer).data

    constructor(fits: Fits, bayerPattern: CfaPattern? = null) : this(fits, bayerPattern, true)

    init {
        if (initialize) {
            val hdu = fits.getImageHDU(0)!!
            val axes = hdu.axes
            val pixels = hdu.kernel as Array<*>
            val bitpix = hdu.bitpix
            val bayer = bayerPattern ?: hdu.header.getStringValue(MaxImDLExt.BAYERPAT)?.trim()?.let(CfaPattern::valueOf)

            if (axes.size == 2) {
                when (bitpix.numberType) {
                    Byte::class.java -> writeByteArray(ImageChannel.GRAY, pixels as Array<ByteArray>)
                    Short::class.java -> writeShortArray(ImageChannel.GRAY, pixels as Array<ShortArray>)
                    Int::class.java -> writeIntArray(ImageChannel.GRAY, pixels as Array<IntArray>)
                    Float::class.java -> writeFloatArray(ImageChannel.GRAY, pixels as Array<FloatArray>)
                    Double::class.java -> writeDoubleArray(ImageChannel.GRAY, pixels as Array<DoubleArray>)
                }
            } else {
                for (channel in RGB_IMAGE_CHANNELS) {
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
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun writePixel(x: Int, y: Int, channel: ImageChannel, color: Float) {
        val index = y * width * 3 + x * 3

        if (channel == ImageChannel.GRAY) {
            this.data[index] = color
            this.data[index + 1] = color
            this.data[index + 2] = color
        } else {
            this.data[index + channel.ordinal] = color
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun readPixel(x: Int, y: Int, channel: ImageChannel): Float {
        val index = y * width * 3 + x * 3
        return this.data[index + if (channel == ImageChannel.GRAY) 0 else channel.ordinal]
    }

    fun writeByteArray(channel: ImageChannel, data: Array<ByteArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x].toInt() and 0xff) / 255f
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeShortArray(channel: ImageChannel, data: Array<ShortArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x].toInt() + 32768) / 65535f
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeIntArray(channel: ImageChannel, data: Array<IntArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = ((data[y][x].toLong() + 2147483648) / 4294967295.0).toFloat()
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeFloatArray(channel: ImageChannel, data: Array<FloatArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = data[y][x]
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeDoubleArray(channel: ImageChannel, data: Array<DoubleArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = data[y][x].toFloat()
                writePixel(x, y, channel, color)
            }
        }
    }

    fun clone(): FitsImage {
        val image = FitsImage(fits)
        data.copyInto(image.data)
        return image
    }

    companion object {

        @JvmStatic private val RGB_IMAGE_CHANNELS = arrayOf(ImageChannel.RED, ImageChannel.GREEN, ImageChannel.BLUE)

        @JvmStatic
        private fun Fits.createColorModel(): ColorModel {
            val space = ColorSpace.getInstance(ColorSpace.CS_sRGB)
            return ComponentColorModel(space, false, false, OPAQUE, DataBuffer.TYPE_BYTE)
        }

        @JvmStatic
        private fun Fits.createRaster(): WritableRaster {
            val width = getImageHDU(0)!!.getNAXIS(1)
            val height = getImageHDU(0)!!.getNAXIS(2)
            val sampleModel = PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, width * 3, intArrayOf(0, 1, 2))
            val buffer = FitsDataBuffer(width * height * 3)
            return Raster.createWritableRaster(sampleModel, buffer, null)
        }
    }
}
