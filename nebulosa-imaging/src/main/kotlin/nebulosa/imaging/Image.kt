package nebulosa.imaging

import nebulosa.fits.imageHDU
import nebulosa.fits.naxis
import nebulosa.imaging.algorithms.CfaPattern.Companion.cfaPattern
import nebulosa.imaging.algorithms.Debayer
import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageData
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.Bitpix
import nom.tam.fits.header.Standard
import nom.tam.util.FitsOutputStream
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.File
import java.io.OutputStream
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
class Image(
    width: Int, height: Int,
    val header: Header,
    val mono: Boolean,
) : BufferedImage(colorModel(mono), raster(width, height, mono), false, null) {

    @JvmField val pixelStride = if (mono) 1 else 3
    @JvmField val stride = width * pixelStride
    @JvmField val data = (raster.dataBuffer as Float8bitsDataBuffer).data

    inline fun indexAt(x: Int, y: Int) = y * stride + x * pixelStride

    inline fun writePixel(x: Int, y: Int, channel: ImageChannel, color: Float) {
        data[indexAt(x, y) + channel.offset] = color
    }

    inline fun readPixel(x: Int, y: Int, channel: ImageChannel): Float {
        return data[indexAt(x, y) + channel.offset]
    }

    inline fun readPixel(x: Int, y: Int): Float {
        return readPixelAt(indexAt(x, y))
    }

    inline fun readPixelAt(index: Int): Float {
        return if (mono) data[index]
        else (data[index] + data[index + 1] + data[index + 2]) / 3f
    }

    fun writeByteArray(channel: ImageChannel, data: Array<ByteArray>): FloatArray {
        var idx = channel.offset

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x].toInt() and 0xff) / 255f
                this.data[idx] = color

                if (color < min) min = color
                else if (color > max) max = color

                idx += pixelStride
            }
        }

        return floatArrayOf(min, max)
    }

    fun writeShortArray(channel: ImageChannel, data: Array<ShortArray>): FloatArray {
        var idx = channel.offset

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = (data[y][x].toInt() + 32768) / 65535f
                this.data[idx] = color

                if (color < min) min = color
                else if (color > max) max = color

                idx += pixelStride
            }
        }

        return floatArrayOf(min, max)
    }

    fun writeIntArray(channel: ImageChannel, data: Array<IntArray>): FloatArray {
        var idx = channel.offset

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = ((data[y][x].toLong() + 2147483648) / 4294967295.0).toFloat()
                this.data[idx] = color

                if (color < min) min = color
                else if (color > max) max = color

                idx += pixelStride
            }
        }

        return floatArrayOf(min, max)
    }

    fun writeFloatArray(channel: ImageChannel, data: Array<FloatArray>): FloatArray {
        var idx = channel.offset

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = data[y][x]
                this.data[idx] = color

                if (color < min) min = color
                else if (color > max) max = color

                idx += pixelStride
            }
        }

        return floatArrayOf(min, max)
    }

    fun writeDoubleArray(channel: ImageChannel, data: Array<DoubleArray>): FloatArray {
        var idx = channel.offset

        var max = Float.MIN_VALUE
        var min = Float.MAX_VALUE

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                val color = data[y][x].toFloat()
                this.data[idx] = color

                if (color < min) min = color
                else if (color > max) max = color

                idx += pixelStride
            }
        }

        return floatArrayOf(min, max)
    }

    fun writeTo(output: IntBuffer) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * stride + x * pixelStride

                if (mono) {
                    val c = (data[index] * 255f).toInt()
                    val p = 0xFF000000.toInt() or (c shl 16) or (c shl 8) or c
                    output.put(p)
                } else {
                    val a = (data[index] * 255f).toInt()
                    val b = (data[index + 1] * 255f).toInt()
                    val c = (data[index + 2] * 255f).toInt()
                    val p = 0xFF000000.toInt() or (a shl 16) or (b shl 8) or c
                    output.put(p)
                }
            }
        }
    }

    fun fits(): Fits {
        val fits = Fits()

        val data = ImageData(header)
        val buffer = FloatArray(this.data.size)
        var idx = 0

        for (i in 0 until pixelStride) {
            for (k in i until this.data.size step pixelStride) {
                buffer[idx++] = this.data[k]
            }
        }

        data.setBuffer(FloatBuffer.wrap(buffer))

        val hdu = ImageHDU(header, data)

        fits.addHDU(hdu)

        return fits
    }

    fun writeAsFits(file: File) {
        file.outputStream().use { writeAsFits(it) }
    }

    fun writeAsFits(outputStream: OutputStream) {
        val fitsOutputStream = FitsOutputStream(outputStream)
        fitsOutputStream.use { fos -> fits().use { it.write(fos) } }
    }

    fun clone(): Image {
        val image = Image(width, height, header, mono)
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
            val buffer = Float8bitsDataBuffer(width * height * pixelStride)
            return Raster.createWritableRaster(sampleModel, buffer, null)
        }

        @JvmStatic
        fun open(file: File): Image {
            return ImageIO.read(file)?.let(::open) ?: Fits(file).use { open(it) }
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun open(
            fits: Fits,
            debayer: Boolean = true,
        ): Image {
            val hdu = requireNotNull(fits.imageHDU(0)) { "The FITS file not contains an image" }

            val header = hdu.header
            val width = header.naxis(1)
            val height = header.naxis(2)
            val mono = hdu.let { it.axes.size != 3 && !(debayer && it.axes.size == 2 && it.cfaPattern != null) }
            val axes = hdu.axes
            val pixels = hdu.kernel as Array<*>
            val bitpix = hdu.bitpix

            // TODO: DATA[i] = BZERO + BSCALE * DATA[i]

            header.setBitpix(Bitpix.FLOAT)

            val image = Image(width, height, header, mono)

            // TODO: This is correct?
            fun mapData(min: Float, max: Float) {
                if (min < 0f || max > 1f) {
                    val k = max - min

                    for (i in image.data.indices) {
                        image.data[i] = (image.data[i] - min) / k
                    }
                }
            }

            if (axes.size == 2) {
                val bayer = hdu.cfaPattern

                val (min, max) = when (val numberType = bitpix.numberType) {
                    Byte::class.java -> image.writeByteArray(ImageChannel.RED, pixels as Array<ByteArray>)
                    Short::class.java -> image.writeShortArray(ImageChannel.RED, pixels as Array<ShortArray>)
                    Int::class.java -> image.writeIntArray(ImageChannel.RED, pixels as Array<IntArray>)
                    Float::class.java -> image.writeFloatArray(ImageChannel.RED, pixels as Array<FloatArray>)
                    Double::class.java -> image.writeDoubleArray(ImageChannel.RED, pixels as Array<DoubleArray>)
                    else -> throw IllegalStateException("invalid bitpix number type: $numberType")
                }

                mapData(min, max)

                if (bayer != null) {
                    Debayer(bayer).transform(image)
                }
            } else {
                var max = Float.MIN_VALUE
                var min = Float.MAX_VALUE

                for (channel in ImageChannel.RGB) {
                    val minMax = when (val numberType = bitpix.numberType) {
                        Byte::class.java -> image.writeByteArray(channel, pixels[channel.offset] as Array<ByteArray>)
                        Short::class.java -> image.writeShortArray(channel, pixels[channel.offset] as Array<ShortArray>)
                        Int::class.java -> image.writeIntArray(channel, pixels[channel.offset] as Array<IntArray>)
                        Float::class.java -> image.writeFloatArray(channel, pixels[channel.offset] as Array<FloatArray>)
                        Double::class.java -> image.writeDoubleArray(channel, pixels[channel.offset] as Array<DoubleArray>)
                        else -> throw IllegalStateException("invalid bitpix number type: $numberType")
                    }

                    min = min(min, minMax[0])
                    max = max(max, minMax[1])
                }

                mapData(min, max)
            }

            return image
        }

        /**
         * Extended image file format support for the Java platform.
         *
         * @see <a href="https://github.com/haraldk/TwelveMonkeys">TwelveMonkeys: Additional plug-ins</a>
         */
        @JvmStatic
        fun open(bufferedImage: BufferedImage): Image {
            val header = Header()
            val width = bufferedImage.width
            val height = bufferedImage.height
            val mono = bufferedImage.type == TYPE_BYTE_GRAY
                    || bufferedImage.type == TYPE_USHORT_GRAY

            header.addValue(Standard.SIMPLE, true)
            header.addValue(Standard.BITPIX, Bitpix.VALUE_FOR_FLOAT)
            header.setNaxes(if (mono) 2 else 3)
            header.setNaxis(1, width)
            header.setNaxis(2, height)
            if (!mono) header.setNaxis(3, 3)
            header.addValue(Standard.BSCALE, 1.0)
            header.addValue(Standard.BZERO, 0.0)
            header.addValue(Standard.EXTEND, true)

            val image = Image(width, height, header, mono)

            var idx = 0

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgb = bufferedImage.getRGB(x, y)

                    if (mono) {
                        // TODO: Fix mono overbrightness when write to file.
                        image.data[idx++] = (rgb and 0xff) / 255f
                    } else {
                        image.data[idx++] = (rgb ushr 16 and 0xff) / 255f
                        image.data[idx++] = (rgb ushr 8 and 0xff) / 255f
                        image.data[idx++] = (rgb and 0xff) / 255f
                    }
                }
            }

            return image
        }
    }
}
