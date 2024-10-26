package nebulosa.fits

import nebulosa.fits.FitsFormat.readPixel
import nebulosa.image.format.ImageChannel
import nebulosa.image.format.ImageData
import nebulosa.io.SeekableSource
import nebulosa.log.di
import nebulosa.log.loggerFor
import okio.Buffer
import okio.Sink
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
internal data class SeekableSourceImageData(
    private val source: SeekableSource,
    private val position: Long,
    override val width: Int,
    override val height: Int,
    override val numberOfChannels: Int,
    private val bitpix: Bitpix,
) : ImageData {

    @JvmField internal val channelSizeInBytes = (numberOfPixels * bitpix.byteLength).toLong()
    @JvmField internal val totalSizeInBytes = channelSizeInBytes * numberOfChannels

    override val red by lazy { readImage(ImageChannel.RED) }

    override val green by lazy { readImage(ImageChannel.GREEN) }

    override val blue by lazy { readImage(ImageChannel.BLUE) }

    @Synchronized
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
        val data = FloatArray(numberOfPixels)
        readChannelTo(channel, data)
        return data
    }

    override fun readChannelTo(channel: ImageChannel, output: FloatArray) {
        // TODO: Read channel from source only if not initialized (remove lazy from red, green and blue).

        val startIndex = channelSizeInBytes * channel.index
        source.seek(position + startIndex)

        var remainingPixels = output.size
        var pos = 0

        Buffer().use { buffer ->
            var min = Float.MAX_VALUE
            var max = Float.MIN_VALUE

            while (remainingPixels > 0) {
                var n = min(PIXEL_COUNT, remainingPixels)
                val byteCount = n * bitpix.byteLength.toLong()

                val size = source.read(buffer, byteCount)

                if (size == 0L) break

                // require(size % bitpix.byteLength == 0L)
                n = (size / bitpix.byteLength).toInt()

                repeat(n) {
                    val pixel = buffer.readPixel(bitpix)
                    if (pixel < min) min = pixel
                    if (pixel > max) max = pixel
                    output[pos++] = pixel
                }

                remainingPixels -= n
            }

            if (min < 0f || max > 1f) {
                val rangeDelta = max - min

                LOG.di("rescaling [{}, {}] to [0, 1]. channel={}, delta={}", min, max, channel, rangeDelta)

                for (i in output.indices) {
                    output[i] = (output[i] - min) / rangeDelta
                }
            }
        }
    }

    internal fun writeTo(sink: Sink): Long {
        var byteCount = 0L

        Buffer().use { buffer ->
            for (i in 0 until numberOfChannels) {
                val startIndex = channelSizeInBytes * i
                var bytesToWrite = channelSizeInBytes

                source.seek(position + startIndex)

                while (bytesToWrite > 0L) {
                    val length = source.read(buffer, min(bytesToWrite, 1024L))

                    if (length > 0L) {
                        sink.write(buffer, length)

                        buffer.clear()

                        byteCount += length
                        bytesToWrite -= length
                    }
                }
            }
        }

        return byteCount
    }

    companion object {

        const val PIXEL_COUNT = 64

        private val LOG = loggerFor<SeekableSourceImageData>()
    }
}
