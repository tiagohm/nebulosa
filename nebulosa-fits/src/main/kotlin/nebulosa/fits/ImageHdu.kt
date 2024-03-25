package nebulosa.fits

import nebulosa.image.format.Header
import nebulosa.image.format.ReadableHeader
import nebulosa.io.SeekableSource
import okio.Buffer
import okio.Sink

data class ImageHdu(
    override var header: Header,
    override var data: Array<out ImageData> = emptyArray(),
) : Hdu<ImageData> {

    val width = header.width
    val height = header.height
    val bitpix = Bitpix.from(header)

    override fun read(source: SeekableSource) {
        val imageSize = (width * height * bitpix.byteSize).toLong()
        var position = source.position
        val n = header.getInt(FitsKeywordDictionary.NAXIS3, 1)
        data = Array(n) { SeekableSourceImageData(source, position, width, height, bitpix).also { position += imageSize } }

        val skipBytes = Hdu.computeRemainingBytesToSkip(imageSize * size)
        if (skipBytes > 0L) source.seek(position + skipBytes)
    }

    override fun write(sink: Sink) {
        header.write(sink)

        val byteCount = data.sumOf { it.writeTo(sink) }
        var remainingBytes = Hdu.computeRemainingBytesToSkip(byteCount)

        if (remainingBytes > 0) {
            Buffer().use {
                while (remainingBytes-- > 0) it.writeByte(0)
                it.readAll(sink)
                it.close()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImageHdu) return false

        if (header != other.header) return false
        if (!data.contentDeepEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + data.contentDeepHashCode()
        return result
    }

    companion object {

        @JvmStatic
        fun isValid(header: ReadableHeader) =
            header.getBoolean(FitsKeywordDictionary.SIMPLE) || header.getStringOrNull(FitsKeywordDictionary.XTENSION) == "IMAGE"
    }
}
