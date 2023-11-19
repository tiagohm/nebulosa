package nebulosa.imaging

import nebulosa.fits.*
import nebulosa.imaging.algorithms.*
import java.awt.color.ColorSpace
import java.awt.image.*
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
class Image(
    width: Int, height: Int,
    val header: Header, val mono: Boolean,
) : BufferedImage(colorModel(mono), raster(width, height, mono), false, null) {

    @JvmField val numberOfChannels = if (mono) 1 else 3
    @JvmField val stride = width
    @JvmField val buffer = raster.dataBuffer as Float8bitsDataBuffer
    @JvmField val size = width * height

    inline val data
        get() = buffer.data

    inline val r
        get() = buffer.r

    inline val g
        get() = buffer.g

    inline val b
        get() = buffer.b

    inline val indices
        get() = 0 until size

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

    fun writeImageData(channel: ImageChannel, data: ImageData) {
        var idx = 0

        when (data.bitpix) {
            Bitpix.BYTE -> data.read {
                while (it.hasRemaining()) {
                    write(idx++, channel, (it.get().toInt() and 0xFF) / 255f)
                }
            }
            Bitpix.SHORT -> data.read {
                while (it.hasRemaining()) {
                    write(idx++, channel, (it.getShort().toInt() + 32768) / 65535f)
                }
            }
            Bitpix.INTEGER -> data.read {
                while (it.hasRemaining()) {
                    write(idx++, channel, ((it.getInt().toLong() + 2147483648) / 4294967295.0).toFloat())
                }
            }
            Bitpix.FLOAT -> data.read {
                while (it.hasRemaining()) {
                    write(idx++, channel, it.getFloat())
                }
            }
            Bitpix.DOUBLE -> data.read {
                while (it.hasRemaining()) {
                    write(idx++, channel, it.getDouble().toFloat())
                }
            }
            Bitpix.LONG -> {
                throw UnsupportedOperationException("BITPIX 64-bit integer is not supported")
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

    fun hdu(): Hdu<ImageData> {
        val data = Array(numberOfChannels) { FloatArrayImageData(width, height, this.data[it]) }
        return ImageHdu(header, data)
    }

    /**
     * Creates a new [Image] and returns a mono version of this image.
     */
    fun mono(grayscale: Grayscale = Grayscale.BT709): Image {
        val image = Image(width, height, header.clone(), true)

        if (mono) {
            r.copyInto(image.r)
        }

        return grayscale.transform(image)
    }

    /**
     * Creates a new [Image] and returns a RGB version of this image.
     */
    fun color(): Image {
        val image = Image(width, height, header.clone(), false)

        if (mono) {
            r.copyInto(image.r)
            r.copyInto(image.g)
            r.copyInto(image.b)
        } else {
            r.copyInto(image.r)
            g.copyInto(image.g)
            b.copyInto(image.b)
        }

        with(image.header) {
            add(Standard.NAXIS, 3)
            add(Standard.NAXIS1, width)
            add(Standard.NAXIS2, height)
            add(Standard.NAXIS3, 3)
        }

        return image
    }

    fun load(fits: Fits, debayer: Boolean = true): Image {
        return load(fits.filterIsInstance<ImageHdu>().first(), debayer)
    }

    fun load(hdu: ImageHdu, debayer: Boolean = true): Image {
        require(hdu.width == width) { "width does not match. $width != ${hdu.width}" }
        require(hdu.height == height) { "height does not match. $height != ${hdu.height}" }

        val mono = isMono(hdu) || !debayer
        require(mono == this.mono) { "color format does not match" }

        load(this, hdu, debayer)

        return this
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
            fits: Fits,
            debayer: Boolean = true,
        ) = open(fits.filterIsInstance<ImageHdu>().first(), debayer)

        @JvmStatic
        fun open(
            hdu: ImageHdu,
            debayer: Boolean = true,
        ): Image {
            val mono = isMono(hdu) || !debayer
            val image = Image(hdu.width, hdu.height, hdu.header, mono)
            load(image, hdu, debayer)
            return image
        }

        @JvmStatic
        private fun load(image: Image, hdu: ImageHdu, debayer: Boolean) {
            val pixels = hdu.data

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

            // TODO: DATA[i] = BZERO + BSCALE * DATA[i]

            // Mono.
            if (hdu.size == 1) {
                val bayer = CfaPattern.from(hdu.header)
                image.writeImageData(ImageChannel.GRAY, pixels[0])

                rescaling()

                if (debayer && bayer != null) {
                    Debayer(bayer).transform(image)
                }
            } else {
                for (channel in ImageChannel.RGB) {
                    image.writeImageData(channel, pixels[channel.offset])
                }

                rescaling()
            }
        }

        @JvmStatic
        fun open(bufferedImage: BufferedImage): Image {
            val header = Header()
            val width = bufferedImage.width
            val height = bufferedImage.height
            val mono = bufferedImage.type == TYPE_BYTE_GRAY
                    || bufferedImage.type == TYPE_USHORT_GRAY

            header.add(Standard.SIMPLE, true)
            header.add(Standard.BITPIX, Bitpix.FLOAT.code)
            header.add(Standard.NAXIS, if (mono) 2 else 3)
            header.add(Standard.NAXISn.n(1), width)
            header.add(Standard.NAXISn.n(2), height)
            if (!mono) header.add(Standard.NAXISn.n(3), 3)
            header.add(Standard.BSCALE, 1.0)
            header.add(Standard.BZERO, 0.0)
            header.add(Standard.EXTEND, true)

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

        @JvmStatic
        fun canDebayer(hdu: ImageHdu): Boolean {
            return hdu.header.cfaPattern != null
        }

        @JvmStatic
        fun isMono(hdu: ImageHdu): Boolean {
            return hdu.size == 1 && !canDebayer(hdu)
        }

        inline fun Image.forEach(
            channel: ImageChannel = ImageChannel.GRAY,
            stepSize: Int = 1,
            computation: (Float) -> Unit,
        ): Int {
            var count = 0

            for (i in indices step stepSize) {
                val pixel = when (channel) {
                    ImageChannel.GRAY -> readGray(i)
                    ImageChannel.RED -> readRed(i)
                    ImageChannel.GREEN -> readGreen(i)
                    ImageChannel.BLUE -> readBlue(i)
                }

                if (pixel >= 0f && pixel.isFinite()) {
                    computation(pixel)
                    count++
                }
            }

            return count
        }
    }
}
