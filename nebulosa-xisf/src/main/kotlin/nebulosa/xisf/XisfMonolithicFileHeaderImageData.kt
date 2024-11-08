package nebulosa.xisf

import nebulosa.image.format.ImageChannel
import nebulosa.image.format.ImageData
import nebulosa.io.SeekableSource
import nebulosa.io.source
import nebulosa.xisf.XisfFormat.readPixel
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

    override val width = image.width
    override val height = image.height
    override val numberOfChannels = image.numberOfChannels

    private val isEmbedded = image.embedded.isNotEmpty()

    private val realSource by lazy { if (isEmbedded) image.embedded.source() else source }

    init {
        val uncompressedSize = image.compressionFormat?.uncompressedSize ?: image.size
        val expectedSize = numberOfChannels * numberOfPixels * image.sampleFormat.byteLength
        check(isEmbedded || uncompressedSize == expectedSize) { "invalid size. $uncompressedSize != $expectedSize" }
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
        val output = FloatArray(numberOfPixels)
        readChannelTo(channel, output)
        return output
    }

    override fun readChannelTo(channel: ImageChannel, output: FloatArray) {
        // TODO: Read channel from source only if not initialized (remove lazy from red, green and blue).
        if (image.pixelStorage == PixelStorageModel.NORMAL) readNormal(channel, output)
        else readPlanar(channel, output)
    }

    /**
     * In the planar storage model, each channel of an image shall be stored as
     * a contiguous sequence of pixel samples (each channel is stored in a separate block).
     */
    private fun readPlanar(channel: ImageChannel, data: FloatArray): FloatArray {
        val startIndex = numberOfPixels * image.sampleFormat.byteLength * channel.index

        realSource.seek(image.position + startIndex)

        var remainingPixels = data.size
        var pos = 0

        var closeable: (() -> Unit)? = null

        val compressedSource = when (image.compressionFormat?.type) {
            CompressionType.ZLIB -> InflaterSource(realSource, Inflater(false).also { closeable = it::end })
            CompressionType.LZ4 -> TODO("Not implemented yet")
            CompressionType.LZ4_HC -> TODO("Not implemented yet")
            CompressionType.ZSTD -> TODO("Not implemented yet")
            null -> realSource
        }

        try {
            Buffer().use { buffer ->
                while (remainingPixels > 0) {
                    var n = min(PIXEL_COUNT, remainingPixels)
                    val byteCount = n * image.sampleFormat.byteLength

                    val size = compressedSource.read(buffer, byteCount)

                    if (size <= 0L) break

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
    private fun readNormal(channel: ImageChannel, data: FloatArray): FloatArray {
        realSource.seek(image.position)

        val blockSizeInBytes = numberOfChannels * image.sampleFormat.byteLength
        val bytesToSkipBefore = channel.index * image.sampleFormat.byteLength
        val bytesToSkipAfter = blockSizeInBytes - bytesToSkipBefore - image.sampleFormat.byteLength
        var remainingPixels = data.size
        var pos = 0

        Buffer().use { buffer ->
            while (remainingPixels > 0) {
                val n = min(PIXEL_COUNT, remainingPixels)
                val byteCount = n * blockSizeInBytes

                check(realSource.read(buffer, byteCount) == byteCount)

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

    companion object {

        const val PIXEL_COUNT = 1024
    }
}
