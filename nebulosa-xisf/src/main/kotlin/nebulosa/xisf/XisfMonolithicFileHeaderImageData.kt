package nebulosa.xisf

import nebulosa.image.format.ImageChannel
import nebulosa.image.format.ImageData
import nebulosa.io.*
import nebulosa.xisf.XisfMonolithicFileHeader.*
import okio.Buffer
import okio.InflaterSource
import java.util.zip.Inflater
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
internal data class XisfMonolithicFileHeaderImageData(
    private val image: Image,
    private val source: SeekableSource,
) : ImageData {

    override val width
        get() = image.width

    override val height
        get() = image.height

    override val numberOfChannels
        get() = image.numberOfChannels

    init {
        val uncompressedSize = image.compressionFormat?.uncompressedSize ?: image.size
        val expectedSize = numberOfChannels * numberOfPixels * image.sampleFormat.byteLength
        check(uncompressedSize == expectedSize) { "invalid size. $uncompressedSize != $expectedSize" }
    }

    override val red by lazy { readImage(ImageChannel.RED) }

    override val green by lazy { readImage(ImageChannel.GREEN) }

    override val blue by lazy { readImage(ImageChannel.BLUE) }

    private fun readImage(channel: ImageChannel): FloatArray {
        return if (numberOfChannels == 1) {
            if (channel.index == 0) readGray() else red
        } else {
            when (channel) {
                ImageChannel.GREEN -> readGreen()
                ImageChannel.BLUE -> readBlue()
                else -> readRed()
            }
        }
    }

    private inline fun readGray(): FloatArray {
        return readChannel(ImageChannel.GRAY)
    }

    private inline fun readRed(): FloatArray {
        return readChannel(ImageChannel.RED)
    }

    private inline fun readGreen(): FloatArray {
        return readChannel(ImageChannel.GREEN)
    }

    private inline fun readBlue(): FloatArray {
        return readChannel(ImageChannel.BLUE)
    }

    private fun readChannel(channel: ImageChannel): FloatArray {
        return if (image.pixelStorage == PixelStorageModel.NORMAL) readNormal(channel)
        else readPlanar(channel)
    }

    /**
     * In the planar storage model, each channel of an image shall be stored as
     * a contiguous sequence of pixel samples (each channel is stored in a separate block).
     */
    private fun readPlanar(channel: ImageChannel): FloatArray {
        val startIndex = numberOfPixels * image.sampleFormat.byteLength * channel.index

        source.seek(image.position + startIndex)

        val data = FloatArray(numberOfPixels)
        var remainingPixels = data.size
        var pos = 0

        var closeable: (() -> Unit)? = null

        val compressedSource = when (image.compressionFormat?.type) {
            CompressionType.ZLIB -> InflaterSource(source, Inflater(false).also { closeable = it::end })
            CompressionType.LZ4 -> TODO("Not implemented yet")
            CompressionType.LZ4_HC -> TODO("Not implemented yet")
            CompressionType.ZSTD -> TODO("Not implemented yet")
            null -> source
        }

        try {
            Buffer().use { buffer ->
                while (remainingPixels > 0) {
                    var n = min(PIXEL_COUNT, remainingPixels)
                    val byteCount = n * image.sampleFormat.byteLength

                    val size = compressedSource.read(buffer, byteCount)

                    if (size == 0L) break

                    // require(size % image.sampleFormat.byteLength == 0L)
                    n = (size / image.sampleFormat.byteLength).toInt()

                    repeat(n) {
                        data[pos++] = buffer.readPixel(image.sampleFormat, image.byteOrder)
                    }

                    remainingPixels -= n
                }
            }
        } finally {
            closeable?.invoke()
        }

        return data
    }

    /**
     * In the normal storage model, all pixel samples of an image shall be stored as
     * a contiguous sequence (all pixel samples are stored in a single block).
     */
    private fun readNormal(channel: ImageChannel): FloatArray {
        source.seek(image.position)

        val pixelBlockSizeInBytes = numberOfChannels * image.sampleFormat.byteLength
        val bytesToSkipBefore = channel.index * image.sampleFormat.byteLength
        val bytesToSkipAfter = pixelBlockSizeInBytes - bytesToSkipBefore - image.sampleFormat.byteLength
        val data = FloatArray(numberOfPixels)
        var remainingPixels = data.size
        var pos = 0

        Buffer().use { buffer ->
            while (remainingPixels > 0) {
                val n = min(PIXEL_COUNT, remainingPixels)
                val byteCount = n * pixelBlockSizeInBytes

                check(source.read(buffer, byteCount) == byteCount)

                repeat(n) {
                    if (bytesToSkipBefore > 0) buffer.skip(bytesToSkipBefore)
                    data[pos++] = buffer.readPixel(image.sampleFormat, image.byteOrder)
                    if (bytesToSkipAfter > 0) buffer.skip(bytesToSkipAfter)
                }

                remainingPixels -= n
            }
        }

        return data
    }

    private fun Buffer.readPixel(format: SampleFormat, byteOrder: ByteOrder): Float {
        return when (format) {
            SampleFormat.UINT8 -> (buffer.readByte().toInt() and 0xFF) / 255f
            SampleFormat.UINT16 -> (buffer.readShort(byteOrder).toInt() and 0xFFFF) / 65535f
            SampleFormat.UINT32 -> ((buffer.readInt(byteOrder).toLong() and 0xFFFFFFFF) / 4294967295.0).toFloat()
            SampleFormat.UINT64 -> TODO("Unsupported UInt64 sample format")
            SampleFormat.FLOAT32 -> buffer.readFloat(byteOrder)
            SampleFormat.FLOAT64 -> buffer.readDouble(byteOrder).toFloat()
        }
    }

    companion object {

        const val PIXEL_COUNT = 64
    }
}
