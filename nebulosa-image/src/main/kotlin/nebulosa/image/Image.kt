package nebulosa.image

import nebulosa.fits.*
import nebulosa.image.algorithms.ComputationAlgorithm
import nebulosa.image.algorithms.TransformAlgorithm
import nebulosa.image.algorithms.transform
import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.algorithms.transformation.Debayer
import nebulosa.image.algorithms.transformation.Grayscale
import nebulosa.image.format.*
import okio.Sink
import java.awt.color.ColorSpace
import java.awt.image.*
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
class Image internal constructor(
    width: Int, height: Int, val mono: Boolean,
    @JvmField val hdu: ImageHdu,
) : BufferedImage(colorModel(mono), raster(hdu, mono), false, null) {

    constructor(width: Int, height: Int, header: ReadableHeader, mono: Boolean)
            : this(width, height, mono, BasicImageHdu(width, height, if (mono) 1 else 3, header, FloatImageData(width, height, if (mono) 1 else 3)))

    constructor(width: Int, height: Int, header: ReadableHeader, red: FloatArray)
            : this(width, height, true, BasicImageHdu(width, height, 1, header, FloatImageData(width, height, 1, red)))

    constructor(width: Int, height: Int, header: ReadableHeader, red: FloatArray, green: FloatArray, blue: FloatArray)
            : this(width, height, false, BasicImageHdu(width, height, 3, header, FloatImageData(width, height, 3, red, green, blue)))

    @JvmField val header = hdu.header
    @JvmField val numberOfChannels = if (mono) 1 else 3
    @JvmField val stride = width
    @JvmField val buffer = raster.dataBuffer as Float8bitsDataBuffer
    @JvmField val size = width * height

    inline val data
        get() = buffer.data

    inline val red
        get() = buffer.red

    inline val green
        get() = buffer.green

    inline val blue
        get() = buffer.blue

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
        red[index] = color
    }

    inline fun writeRed(x: Int, y: Int, color: Float) {
        writeRed(indexAt(x, y), color)
    }

    inline fun writeGreen(index: Int, color: Float) {
        green[index] = color
    }

    inline fun writeGreen(x: Int, y: Int, color: Float) {
        writeGreen(indexAt(x, y), color)
    }

    inline fun writeBlue(index: Int, color: Float) {
        blue[index] = color
    }

    inline fun writeBlue(x: Int, y: Int, color: Float) {
        writeBlue(indexAt(x, y), color)
    }

    inline fun writeGray(index: Int, color: Float) {
        red[index] = color

        if (!mono) {
            green[index] = color
            blue[index] = color
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
        return red[index]
    }

    inline fun readRed(x: Int, y: Int): Float {
        return readRed(indexAt(x, y))
    }

    inline fun readGreen(index: Int): Float {
        return green[index]
    }

    inline fun readGreen(x: Int, y: Int): Float {
        return readGreen(indexAt(x, y))
    }

    inline fun readBlue(index: Int): Float {
        return blue[index]
    }

    inline fun readBlue(x: Int, y: Int): Float {
        return readBlue(indexAt(x, y))
    }

    inline fun readGray(index: Int): Float {
        return if (mono) red[index] else (red[index] + green[index] + blue[index]) / 3f
    }

    inline fun readGrayBT709(index: Int): Float {
        return if (mono) red[index] else (red[index] * 0.2125f + green[index] * 0.7154f + blue[index] * 0.0721f)
    }

    inline fun readGray(x: Int, y: Int): Float {
        return readGray(indexAt(x, y))
    }

    fun writeTo(output: IntArray) {
        var idx = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = indexAt(x, y)

                if (mono) {
                    val c = (red[index] * 255f).toInt()
                    val p = 0xFF000000.toInt() or (c shl 16) or (c shl 8) or c
                    output[idx++] = p
                } else {
                    val ri = (red[index] * 255f).toInt()
                    val gi = (green[index] * 255f).toInt()
                    val bi = (blue[index] * 255f).toInt()
                    val p = 0xFF000000.toInt() or (ri shl 16) or (gi shl 8) or bi
                    output[idx++] = p
                }
            }
        }
    }

    fun writeTo(sink: Sink, format: ImageFormat) {
        format.write(sink, listOf(hdu))
    }

    /**
     * Creates a new [Image] and returns a mono version of this image.
     */
    fun mono(grayscale: Grayscale = Grayscale.BT709): Image {
        return if (mono) {
            Image(width, height, FitsHeader(header), red.clone())
        } else {
            grayscale.transform(this)
        }
    }

    /**
     * Creates a new [Image] and returns a RGB version of this image.
     */
    fun color(): Image {
        val newHeader = FitsHeader(header)

        val image = if (mono) {
            Image(width, height, newHeader, red.clone(), red.clone(), red.clone())
        } else {
            Image(width, height, newHeader, red.clone(), green.clone(), blue.clone())
        }

        with(newHeader) {
            add(FitsKeyword.NAXIS, 3)
            add(FitsKeyword.NAXIS1, width)
            add(FitsKeyword.NAXIS2, height)
            add(FitsKeyword.NAXIS3, 3)
        }

        return image
    }

    fun canLoad(hdu: ImageHdu, debayer: Boolean = true): Boolean {
        return hdu.width == width && hdu.height == height && (isMono(hdu) || !debayer) == mono
    }

    fun canLoad(image: ImageRepresentation, debayer: Boolean = true): Boolean {
        return canLoad(image.filterIsInstance<ImageHdu>().first(), debayer)
    }

    fun load(image: ImageRepresentation, debayer: Boolean = true): Image? {
        return load(image.filterIsInstance<ImageHdu>().first(), debayer)
    }

    fun load(hdu: ImageHdu, debayer: Boolean = true): Image? {
        if (!canLoad(hdu, debayer)) return null
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
        internal fun raster(width: Int, height: Int, mono: Boolean, red: FloatArray, green: FloatArray, blue: FloatArray): WritableRaster {
            val pixelStride = if (mono) 1 else 3
            val bandOffsets = if (mono) intArrayOf(0) else intArrayOf(0, 1, 2)
            val sampleModel = PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, pixelStride, width * pixelStride, bandOffsets)
            val buffer = Float8bitsDataBuffer(mono, red, green, blue)
            return Raster.createWritableRaster(sampleModel, buffer, null)
        }

        @JvmStatic
        internal fun raster(hdu: ImageHdu, mono: Boolean): WritableRaster {
            return raster(hdu.width, hdu.height, isMono(hdu) || mono, hdu.data.red, hdu.data.green, hdu.data.blue)
        }

        @JvmStatic
        fun open(image: ImageRepresentation, debayer: Boolean = true): Image {
            return open(image.filterIsInstance<ImageHdu>().first(), debayer)
        }

        @JvmStatic
        fun open(hdu: ImageHdu, debayer: Boolean = true): Image {
            val image = Image(hdu.width, hdu.height, isMono(hdu) || !debayer, hdu)

            if (image.mono && debayer) {
                image.debayer()
            }

            return image
        }

        private inline fun Image.debayer(bayer: CfaPattern? = CfaPattern.from(header)) {
            if (bayer != null) {
                Debayer(bayer).transform(this)
            }
        }

        @JvmStatic
        private fun load(image: Image, hdu: ImageHdu, debayer: Boolean) {
            hdu.data.red.copyInto(image.red)

            if (!image.mono) {
                hdu.data.green.copyInto(image.green)
                hdu.data.blue.copyInto(image.blue)

                if (debayer) {
                    image.debayer()
                }
            }
        }

        @JvmStatic
        private fun load(image: Image, debayer: Boolean) {
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
            if (image.mono) {
                rescaling()
            } else {
                val bayer = CfaPattern.from(image.header)

                if (debayer && bayer != null) {
                    Debayer(bayer).transform(image)
                }

                rescaling()
            }
        }

        @JvmStatic
        fun open(bufferedImage: BufferedImage): Image {
            val header = FitsHeader()
            val width = bufferedImage.width
            val height = bufferedImage.height
            val mono = bufferedImage.type == TYPE_BYTE_GRAY
                    || bufferedImage.type == TYPE_USHORT_GRAY

            header.add(FitsKeyword.SIMPLE, true)
            header.add(FitsKeyword.BITPIX, Bitpix.FLOAT.code)
            header.add(FitsKeyword.NAXIS, if (mono) 2 else 3)
            header.add(FitsKeyword.NAXISn.n(1), width)
            header.add(FitsKeyword.NAXISn.n(2), height)
            if (!mono) header.add(FitsKeyword.NAXISn.n(3), 3)
            header.add(FitsKeyword.BSCALE, 1.0)
            header.add(FitsKeyword.BZERO, 0.0)
            header.add(FitsKeyword.EXTEND, true)

            val image = Image(width, height, header, mono)

            var idx = 0

            // TODO: Fix mono overbrightness when write to file.
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgb = bufferedImage.getRGB(x, y)

                    if (mono) {
                        image.red[idx++] = (rgb and 0xff) / 255f
                    } else {
                        image.red[idx] = (rgb ushr 16 and 0xff) / 255f
                        image.green[idx] = (rgb ushr 8 and 0xff) / 255f
                        image.blue[idx++] = (rgb and 0xff) / 255f
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
            return hdu.isMono && !canDebayer(hdu)
        }

        inline fun Image.forEach(
            channel: ImageChannel = ImageChannel.GRAY,
            stepSize: Int = 1,
            computation: (Float) -> Unit,
        ): Int {
            var count = 0

            for (i in 0 until size step stepSize) {
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
