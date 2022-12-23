package nebulosa.imaging

import java.awt.color.ColorSpace
import java.awt.image.*

open class Image(
    width: Int, height: Int,
    val mono: Boolean,
) : BufferedImage(colorModel(mono), raster(width, height, mono), false, null) {

    @JvmField val pixelStride = if (mono) 1 else 3

    @JvmField val stride = width * pixelStride

    @JvmField val data = (raster.dataBuffer as Float8bppDataBuffer).data

    @Suppress("NOTHING_TO_INLINE")
    inline fun writePixel(x: Int, y: Int, channel: ImageChannel, color: Float) {
        val index = y * width * pixelStride + x * pixelStride
        this.data[index + channel.offset] = color
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun readPixel(x: Int, y: Int, channel: ImageChannel): Float {
        val index = y * width * pixelStride + x * pixelStride
        return this.data[index + channel.offset]
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
                writePixel(x, y, channel, data[y][x])
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

    open fun clone(): Image {
        val image = Image(width, height, mono)
        data.copyInto(image.data)
        return image
    }

    companion object {

        @JvmStatic
        internal fun colorModel(mono: Boolean): ColorModel {
            val space = ColorSpace.getInstance(if (mono) ColorSpace.CS_GRAY else ColorSpace.CS_sRGB)
            return ComponentColorModel(space, false, false, OPAQUE, DataBuffer.TYPE_BYTE)
        }

        @JvmStatic
        internal fun raster(width: Int, height: Int, mono: Boolean): WritableRaster {
            val pixelStride = if (mono) 1 else 3
            val bandOffsets = if (mono) intArrayOf(0) else intArrayOf(0, 1, 2)
            val sampleModel = PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, pixelStride, width * pixelStride, bandOffsets)
            val buffer = Float8bppDataBuffer(width * height * pixelStride)
            return Raster.createWritableRaster(sampleModel, buffer, null)
        }
    }
}
