package nebulosa.imaging

import nebulosa.fits.FitsKeywords
import nebulosa.fits.imageHDU
import nebulosa.fits.naxis
import nebulosa.imaging.algorithms.*
import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageData
import nom.tam.fits.ImageHDU
import nom.tam.fits.header.Bitpix
import nom.tam.util.FitsOutputStream
import java.awt.color.ColorSpace
import java.awt.image.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.FloatBuffer
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.outputStream
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
class Image(
    width: Int, height: Int,
    header: Header,
    val mono: Boolean,
) : BufferedImage(colorModel(mono), raster(width, height, mono), false, null) {

    @JvmField val numberOfChannels = if (mono) 1 else 3
    @JvmField val stride = width
    @JvmField val buffer = raster.dataBuffer as Float8bitsDataBuffer
    @JvmField val data = buffer.data
    @JvmField val r = buffer.r
    @JvmField val g = buffer.g
    @JvmField val b = buffer.b

    var header = header
        private set

    val size = width * height

    val indices = 0 until size

    inline fun indexAt(x: Int, y: Int): Int {
        return y * stride + x
    }

    inline fun write(index: Int, channel: ImageChannel, color: Float) {
        write(index, channel.offset, color)
    }

    inline fun write(index: Int, channel: Int, color: Float) {
        data[channel][index] = color
    }

    inline fun write(x: Int, y: Int, channel: ImageChannel, color: Float) {
        write(x, y, channel.offset, color)
    }

    inline fun write(x: Int, y: Int, channel: Int, color: Float) {
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
        return read(index, channel.offset)
    }

    inline fun read(index: Int, channel: Int): Float {
        return data[channel][index]
    }

    inline fun read(x: Int, y: Int, channel: ImageChannel): Float {
        return read(x, y, channel.offset)
    }

    inline fun read(x: Int, y: Int, channel: Int): Float {
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
        file.outputStream().use(::writeAsFits)
    }

    fun writeAsFits(path: Path) {
        path.outputStream().use(::writeAsFits)
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
        // TODO: Clone header and set RGB info.
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

    fun <T> compute(algorithm: ComputationAlgorithm<T>) = algorithm.compute(this)

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
        fun open(
            path: Path,
            debayer: Boolean = true,
            onlyHeaders: Boolean = false,
            output: Image? = null,
        ) = open(path.toFile(), debayer, onlyHeaders, output)

        @JvmStatic
        fun open(
            file: File,
            debayer: Boolean = true,
            onlyHeaders: Boolean = false,
            output: Image? = null,
        ) = ImageIO.read(file)?.let(::openImage)
            ?: Fits(file).use { openFITS(it, debayer, onlyHeaders, output) }

        @JvmStatic
        fun openFITS(
            inputStream: InputStream,
            debayer: Boolean = true,
            onlyHeaders: Boolean = false,
            output: Image? = null,
        ) = Fits(inputStream).use { openFITS(it, debayer, onlyHeaders, output) }

        @JvmStatic
        fun openImage(inputStream: InputStream): Image? {
            return ImageIO.read(inputStream)?.let(this::openImage)
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun openFITS(
            fits: Fits,
            debayer: Boolean = true,
            onlyHeaders: Boolean = false,
            output: Image? = null,
        ): Image {
            val hdu = requireNotNull(fits.imageHDU(0)) { "The FITS file not contains an image" }

            val header = hdu.header
            val width = header.naxis(1)
            val height = header.naxis(2)
            val mono = isMono(header) || !debayer
            val axes = hdu.axes
            val bitpix = hdu.bitpix

            if (output != null) {
                require(output.width == width) { "output width [${output.width}] dont match: [$width]" }
                require(output.height == height) { "output height [${output.height}] dont match: [$height]" }
                require(output.mono == mono) { "output mono [${output.mono}] dont match: [$mono]" }
            }

            // TODO: DATA[i] = BZERO + BSCALE * DATA[i]

            header.addValue(FitsKeywords.BITPIX, Bitpix.VALUE_FOR_FLOAT)

            val image = output ?: Image(width, height, header, mono)
            image.header = header

            if (onlyHeaders) return image

            val pixels = hdu.kernel as Array<*>

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

            // Mono.
            if (axes.size == 2) {
                val bayer = CfaPattern.of(hdu)

                when (val numberType = bitpix.numberType) {
                    Byte::class.java -> image.writeByteArray(ImageChannel.RED, pixels as Array<ByteArray>)
                    Short::class.java -> image.writeShortArray(ImageChannel.RED, pixels as Array<ShortArray>)
                    Int::class.java -> image.writeIntArray(ImageChannel.RED, pixels as Array<IntArray>)
                    Float::class.java -> image.writeFloatArray(ImageChannel.RED, pixels as Array<FloatArray>)
                    Double::class.java -> image.writeDoubleArray(ImageChannel.RED, pixels as Array<DoubleArray>)
                    else -> throw IllegalStateException("invalid bitpix number type: $numberType")
                }

                rescaling()

                if (debayer && bayer != null) {
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

        @JvmStatic
        fun openImage(bufferedImage: BufferedImage, output: Image? = null): Image {
            val header = Header()
            val width = bufferedImage.width
            val height = bufferedImage.height
            val mono = bufferedImage.type == TYPE_BYTE_GRAY
                    || bufferedImage.type == TYPE_USHORT_GRAY

            if (output != null) {
                require(output.width == width) { "output width [${output.width}] dont match: [$width]" }
                require(output.height == height) { "output height [${output.height}] dont match: [$height]" }
                require(output.mono == mono) { "output mono [${output.mono}] dont match: [$mono]" }
            }

            header.addValue(FitsKeywords.SIMPLE, true)
            header.addValue(FitsKeywords.BITPIX, Bitpix.VALUE_FOR_FLOAT)
            header.addValue(FitsKeywords.NAXIS, if (mono) 2 else 3)
            header.addValue(FitsKeywords.NAXISn.n(1), width)
            header.addValue(FitsKeywords.NAXISn.n(2), height)
            if (!mono) header.addValue(FitsKeywords.NAXISn.n(3), 3)
            header.addValue(FitsKeywords.BSCALE, 1.0)
            header.addValue(FitsKeywords.BZERO, 0.0)
            header.addValue(FitsKeywords.EXTEND, true)

            val image = output ?: Image(width, height, header, mono)
            image.header = header

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

        @JvmStatic
        fun isMono(header: Header): Boolean {
            return header.naxis() != 3 && !(header.naxis() == 2 && CfaPattern.of(header) != null)
        }
    }
}
