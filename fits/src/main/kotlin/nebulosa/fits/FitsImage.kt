package nebulosa.fits

import nom.tam.fits.Fits
import nom.tam.fits.header.extra.MaxImDLExt
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

@Suppress("UNCHECKED_CAST")
open class FitsImage private constructor(
    val fits: Fits,
    bayerPattern: CfaPattern? = null,
    initialize: Boolean = true,
) : BufferedImage(fits.getImageHDU(0)!!.getNAXIS(1), fits.getImageHDU(0)!!.getNAXIS(2), TYPE_INT_RGB) {

    val data: IntArray = (raster.dataBuffer as DataBufferInt).data

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
    inline fun writePixel(x: Int, y: Int, channel: ImageChannel, color: Int) {
        val index = y * width + x
        val pixel = this.data[index]

        this.data[index] = when (channel) {
            ImageChannel.GRAY -> (color shl 16) or (color shl 8) or color
            ImageChannel.RED -> (pixel and 0x00FFFF) or (color shl 16)
            ImageChannel.GREEN -> (pixel and 0xFF00FF) or (color shl 8)
            ImageChannel.BLUE -> (pixel and 0xFFFF00) or color
        }
    }

    fun writeByteArray(channel: ImageChannel, data: Array<ByteArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = data[y][x].toInt() and 0xff
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeShortArray(channel: ImageChannel, data: Array<ShortArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x].toInt() + 32768) ushr 8
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeIntArray(channel: ImageChannel, data: Array<IntArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = ((data[y][x] + 2147483648) ushr 24).toInt()
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeFloatArray(channel: ImageChannel, data: Array<FloatArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x] * 255f).toInt()
                writePixel(x, y, channel, color)
            }
        }
    }

    fun writeDoubleArray(channel: ImageChannel, data: Array<DoubleArray>) {
        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x] * 255.0).toInt()
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
    }
}
