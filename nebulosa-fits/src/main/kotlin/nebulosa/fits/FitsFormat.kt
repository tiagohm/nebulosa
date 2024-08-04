package nebulosa.fits

import nebulosa.fits.FitsHeader.Companion.isFirstCard
import nebulosa.image.format.*
import nebulosa.io.*
import nebulosa.log.loggerFor
import okio.Buffer
import okio.BufferedSource
import okio.Sink
import java.io.EOFException
import kotlin.math.max

data object FitsFormat : ImageFormat {

    const val SIGNATURE = "SIMPLE"
    const val BLOCK_SIZE = 2880

    @JvmStatic
    fun computeRemainingBytesToSkip(sizeInBytes: Long): Long {
        val numberOfBlocks = (sizeInBytes / BLOCK_SIZE) + 1
        val remainingByteCount = (numberOfBlocks * BLOCK_SIZE) - sizeInBytes
        return max(0L, remainingByteCount)
    }

    @JvmStatic
    fun BufferedSource.readSignature() = readString(6L, Charsets.US_ASCII)

    fun isImageHdu(header: ReadableHeader) =
        header.getBoolean(FitsKeyword.SIMPLE) || header.getStringOrNull(FitsKeyword.XTENSION) == "IMAGE"

    fun readHeader(source: SeekableSource): FitsHeader {
        val header = FitsHeader()

        var count = 0

        Buffer().use { buffer ->
            while (true) {
                buffer.clear()

                if (source.read(buffer, 80L) != 80L) throw EOFException()

                count++

                val card = try {
                    FitsHeaderCard.from(buffer)
                } catch (e: IllegalArgumentException) {
                    break
                }

                if (header.isEmpty()) {
                    require(isFirstCard(card.key)) { "Not a proper FITS header: ${card.key}" }
                } else if (card.isBlank) {
                    continue
                } else if (card.key == FitsKeyword.END.key) {
                    break
                }

                header.add(card)
            }

            val skipBytes = computeRemainingBytesToSkip(count * 80L)
            if (skipBytes > 0L) source.skip(skipBytes)
        }

        return header
    }

    fun readImageData(header: ReadableHeader, source: SeekableSource): ImageData {
        val width = header.width
        val height = header.height
        val numberOfChannels = header.numberOfChannels
        val bitpix = header.bitpix
        val position = source.position

        val data = SeekableSourceImageData(source, position, width, height, numberOfChannels, bitpix)
        val skipBytes = computeRemainingBytesToSkip(data.totalSizeInBytes)
        if (skipBytes > 0L) source.seek(position + data.totalSizeInBytes + skipBytes)

        return data
    }

    override fun read(source: SeekableSource): List<Hdu<*>> {
        val hdus = ArrayList<ImageHdu>(1)

        while (!source.exhausted) {
            val header = try {
                readHeader(source)
            } catch (e: Throwable) {
                LOG.error("failed to read FITS header", e)
                break
            }

            val hdu = when {
                isImageHdu(header) -> BasicImageHdu(header.width, header.height, header.numberOfChannels, header, readImageData(header, source))
                else -> {
                    LOG.warn("unsupported FITS header: {}", header)
                    continue
                }
            }

            hdus.add(hdu)
        }

        return hdus
    }

    fun writeHeader(header: ReadableHeader, bitpix: Bitpix, sink: Sink) {
        Buffer().use { buffer ->
            for (card in header) {
                if (card.key == bitpix.key) buffer.writeCard(bitpix)
                else buffer.writeCard(card)
            }

            if (header.last().key != FitsHeaderCard.END.key) {
                buffer.writeString(FitsHeaderCard.END.formatted(), Charsets.US_ASCII)
            }

            val remainingBytes = computeRemainingBytesToSkip(buffer.size)
            repeat(remainingBytes.toInt()) { buffer.writeByte(0) }
            buffer.readAll(sink)
        }
    }

    fun writeImageData(data: ImageData, bitpix: Bitpix, sink: Sink) {
        val channels = arrayOf(data.red, data.green, data.blue)
        var byteCount = 0L

        Buffer().use { buffer ->
            for (channel in 0 until data.numberOfChannels) {
                for (i in 0 until data.numberOfPixels) {
                    buffer.writePixel(channels[channel][i], bitpix)

                    if (buffer.size >= 1024L) {
                        byteCount += buffer.readAll(sink)
                    }
                }
            }

            byteCount += buffer.readAll(sink)
            val remainingBytes = computeRemainingBytesToSkip(byteCount)

            if (remainingBytes > 0) {
                repeat(remainingBytes.toInt()) { buffer.writeByte(0) }
                buffer.readAll(sink)
            }
        }
    }

    override fun write(sink: Sink, hdus: Iterable<Hdu<*>>, modifier: ImageModifier) {
        val bitpix = modifier.bitpix()

        for (hdu in hdus) {
            if (hdu is ImageHdu) {
                with(bitpix ?: hdu.header.bitpix) {
                    writeHeader(hdu.header, this, sink)
                    writeImageData(hdu.data, this, sink)
                }
            }
        }
    }

    @JvmStatic
    internal fun Buffer.readPixel(bitpix: Bitpix): Float {
        return when (bitpix) {
            Bitpix.BYTE -> (readByte().toInt() and 0xFF) / 255f
            Bitpix.SHORT -> (readShort().toInt() + 32768) / 65535f
            Bitpix.INTEGER -> ((readInt().toLong() + 2147483648L) / 4294967295.0).toFloat()
            Bitpix.LONG -> TODO("Unsupported UInt64 sample format")
            Bitpix.FLOAT -> readFloat()
            Bitpix.DOUBLE -> readDouble().toFloat()
        }
    }

    @JvmStatic
    internal fun Buffer.writePixel(pixel: Float, bitpix: Bitpix) {
        when (bitpix) {
            Bitpix.BYTE -> writeByte((pixel * 255f).toInt())
            Bitpix.SHORT -> writeShort((pixel * 65535f).toInt() - 32768)
            Bitpix.INTEGER -> writeInt(((pixel * 4294967295.0).toLong() - 2147483648L).toInt())
            Bitpix.LONG -> TODO("Unsupported 64-bit format")
            Bitpix.FLOAT -> writeFloat(pixel)
            Bitpix.DOUBLE -> writeDouble(pixel.toDouble())
        }
    }

    @JvmStatic
    private fun Buffer.writeCard(card: HeaderCard) {
        writeString(card.formatted(), Charsets.US_ASCII)
    }

    @JvmStatic private val LOG = loggerFor<FitsFormat>()
}
