package nebulosa.imaging

import nebulosa.fits.imageHDU
import nebulosa.fits.naxis
import nebulosa.imaging.algorithms.CfaPattern.Companion.cfaPattern
import nebulosa.imaging.algorithms.Debayer
import nebulosa.imaging.algorithms.TransformAlgorithm
import nebulosa.imaging.algorithms.TransformAlgorithm.Companion.transform
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
import java.io.InputStream
import java.io.OutputStream
import java.nio.FloatBuffer
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
class Image(
    width: Int, height: Int,
    val header: Header,
    val mono: Boolean,
) : BufferedImage(colorModel(mono), raster(width, height, mono), false, null) {

    @JvmField val numberOfChannels = if (mono) 1 else 3
    @JvmField val stride = width
    @JvmField val buffer = raster.dataBuffer as Float8bitsDataBuffer
    @JvmField val data = buffer.data
    @JvmField val r = buffer.r
    @JvmField val g = buffer.g
    @JvmField val b = buffer.b

    inline fun indexAt(x: Int, y: Int): Int {
        return y * stride + x
    }

    inline fun write(index: Int, channel: ImageChannel, color: Float) {
        data[channel.offset][index] = color
    }

    inline fun write(x: Int, y: Int, channel: ImageChannel, color: Float) {
        write(indexAt(x, y), channel, color)
    }

    inline fun writeRed(index: Int, color: Float) {
        r[index] = color
    }

    inline fun writeRed(x: Int, y: Int, color: Float) {
        writeRed(indexAt(x, y), color)
    }

    inline fun writeGreen(index: Int, color: Float) {
        g[index] = color
    }

    inline fun writeGreen(x: Int, y: Int, color: Float) {
        writeGreen(indexAt(x, y), color)
    }

    inline fun writeBlue(index: Int, color: Float) {
        b[index] = color
    }

    inline fun writeBlue(x: Int, y: Int, color: Float) {
        writeBlue(indexAt(x, y), color)
    }

    inline fun writeGray(index: Int, color: Float) {
        r[index] = color

        if (!mono) {
            g[index] = color
            b[index] = color
        }
    }

    inline fun writeGray(x: Int, y: Int, color: Float) {
        writeGray(indexAt(x, y), color)
    }

    inline fun read(index: Int, channel: ImageChannel): Float {
        return data[channel.offset][index]
    }

    inline fun read(x: Int, y: Int, channel: ImageChannel): Float {
        return read(indexAt(x, y), channel)
    }

    inline fun readRed(index: Int): Float {
        return r[index]
    }

    inline fun readRed(x: Int, y: Int): Float {
        return readRed(indexAt(x, y))
    }

    inline fun readGreen(index: Int): Float {
        return g[index]
    }

    inline fun readGreen(x: Int, y: Int): Float {
        return readGreen(indexAt(x, y))
    }

    inline fun readBlue(index: Int): Float {
        return b[index]
    }

    inline fun readBlue(x: Int, y: Int): Float {
        return readBlue(indexAt(x, y))
    }

    inline fun readGray(index: Int): Float {
        return if (mono) r[index] else (r[index] + g[index] + b[index]) / 3f
    }

    inline fun readGray(x: Int, y: Int): Float {
        return readGray(indexAt(x, y))
    }

    fun writeByteArray(channel: ImageChannel, data: Array<ByteArray>) {
        var idx = 0

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                write(idx++, channel, (data[y][x].toInt() and 0xff) / 255f)
            }
        }
    }

    fun writeShortArray(channel: ImageChannel, data: Array<ShortArray>) {
        var idx = 0

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                write(idx++, channel, (data[y][x].toInt() + 32768) / 65535f)
            }
        }
    }

    fun writeIntArray(channel: ImageChannel, data: Array<IntArray>) {
        var idx = 0

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                write(idx++, channel, ((data[y][x].toLong() + 2147483648) / 4294967295.0).toFloat())
            }
        }
    }

    fun writeFloatArray(channel: ImageChannel, data: Array<FloatArray>) {
        var idx = 0

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                write(idx++, channel, data[y][x])
            }
        }
    }

    fun writeDoubleArray(channel: ImageChannel, data: Array<DoubleArray>) {
        var idx = 0

        for (y in data.indices) {
            for (x in 0 until data[y].size) {
                write(idx++, channel, data[y][x].toFloat())
            }
        }
    }

    fun writeTo(output: IntArray) {
        var idx = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = indexAt(x, y)

                if (mono) {
                    val c = (r[index] * 255f).toInt()
                    val p = 0xFF000000.toInt() or (c shl 16) or (c shl 8) or c
                    output[idx++] = p
                } else {
                    val ri = (r[index] * 255f).toInt()
                    val gi = (g[index] * 255f).toInt()
                    val bi = (b[index] * 255f).toInt()
                    val p = 0xFF000000.toInt() or (ri shl 16) or (gi shl 8) or bi
                    output[idx++] = p
                }
            }
        }
    }

    fun fits(): Fits {
        val fits = Fits()

        val data = ImageData(header)
        val buffer = FloatArray(buffer.size)
        val size = width * height

        r.copyInto(buffer, 0)

        if (!mono) {
            g.copyInto(buffer, size)
            b.copyInto(buffer, size * 2)
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

    /**
     * Creates a new [Image] and returns a mono version of this image.
     */
    fun mono(): Image {
        val image = Image(width, height, header, true)

        if (mono) {
            r.copyInto(image.r)
        } else {
            for (i in r.indices) {
                image.r[i] = (r[i] + b[i] + g[i]) / 3f
            }
        }

        return image
    }

    /**
     * Creates a new [Image] and returns a RGB version of this image.
     */
    fun color(): Image {
        val image = Image(width, height, header, false)

        if (mono) {
            r.copyInto(image.r)
            r.copyInto(image.g)
            r.copyInto(image.b)
        } else {
            r.copyInto(image.r)
            g.copyInto(image.g)
            b.copyInto(image.b)
        }

        return image
    }

    fun clone() = if (mono) mono() else color()

    fun transform(vararg algorithms: TransformAlgorithm) = algorithms.transform(this)

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
            val size = width * height

            val buffer = if (mono) Float8bitsDataBuffer.mono(size)
            else Float8bitsDataBuffer.rgb(size)

            return Raster.createWritableRaster(sampleModel, buffer, null)
        }

        @JvmStatic
        fun open(file: File): Image {
            return ImageIO.read(file)?.let(::open) ?: Fits(file).use { open(it) }
        }

        @JvmStatic
        fun open(inputStream: InputStream): Image {
            return ImageIO.read(inputStream)?.let(::open) ?: Fits(inputStream).use { open(it) }
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

            fun rescaling() {
                for (p in 0 until image.numberOfChannels) {
                    val minMax = floatArrayOf(Float.MAX_VALUE, Float.MIN_VALUE)
                    val plane = image.data[p]

                    for (i in plane.indices) {
                        val k = plane[i]
                        minMax[0] = min(minMax[0], k)
                        minMax[1] = max(minMax[1], k)
                    }

                    if (minMax[0] < 0f || minMax[1] > 1f) {
                        val k = minMax[1] - minMax[0]

                        for (i in plane.indices) {
                            plane[i] = (plane[i] - minMax[0]) / k
                        }
                    }
                }
            }

            if (axes.size == 2) {
                val bayer = hdu.cfaPattern

                when (val numberType = bitpix.numberType) {
                    Byte::class.java -> image.writeByteArray(ImageChannel.RED, pixels as Array<ByteArray>)
                    Short::class.java -> image.writeShortArray(ImageChannel.RED, pixels as Array<ShortArray>)
                    Int::class.java -> image.writeIntArray(ImageChannel.RED, pixels as Array<IntArray>)
                    Float::class.java -> image.writeFloatArray(ImageChannel.RED, pixels as Array<FloatArray>)
                    Double::class.java -> image.writeDoubleArray(ImageChannel.RED, pixels as Array<DoubleArray>)
                    else -> throw IllegalStateException("invalid bitpix number type: $numberType")
                }

                rescaling()

                if (bayer != null) {
                    Debayer(bayer).transform(image)
                }
            } else {
                for (channel in ImageChannel.RGB) {
                    when (val numberType = bitpix.numberType) {
                        Byte::class.java -> image.writeByteArray(channel, pixels[channel.offset] as Array<ByteArray>)
                        Short::class.java -> image.writeShortArray(channel, pixels[channel.offset] as Array<ShortArray>)
                        Int::class.java -> image.writeIntArray(channel, pixels[channel.offset] as Array<IntArray>)
                        Float::class.java -> image.writeFloatArray(channel, pixels[channel.offset] as Array<FloatArray>)
                        Double::class.java -> image.writeDoubleArray(channel, pixels[channel.offset] as Array<DoubleArray>)
                        else -> throw IllegalStateException("invalid bitpix number type: $numberType")
                    }
                }

                rescaling()
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

            // TODO: Fix mono overbrightness when write to file.
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgb = bufferedImage.getRGB(x, y)

                    if (mono) {
                        image.r[idx++] = (rgb and 0xff) / 255f
                    } else {
                        image.r[idx] = (rgb ushr 16 and 0xff) / 255f
                        image.g[idx] = (rgb ushr 8 and 0xff) / 255f
                        image.b[idx++] = (rgb and 0xff) / 255f
                    }
                }
            }

            return image
        }
    }
}
