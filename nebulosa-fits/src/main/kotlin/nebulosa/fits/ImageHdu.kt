package nebulosa.fits

import java.nio.channels.SeekableByteChannel

data class ImageHdu(
    override var header: Header,
    override var data: Array<out ImageData> = emptyArray(),
) : Hdu<ImageData> {

    val width = header.getInt(Standard.NAXIS1, 0)

    val height = header.getInt(Standard.NAXIS2, 0)

    val bitpix = Bitpix.from(header)

    override fun read(source: SeekableByteChannel) {
        val imageSize = (width * height * bitpix.byteSize).toLong()
        var position = source.position()

        val n = header.getInt(Standard.NAXIS3, 1)
        data = Array(n) { SeekableByteChannelImageData(source, position, width, height, bitpix).also { position += imageSize } }

        val skipBytes = Hdu.computeRemainingBytesToSkip(imageSize * size)
        if (skipBytes > 0L) source.position(position + skipBytes)
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
        fun isValid(header: Header) = header.getBoolean(Standard.SIMPLE)
    }
}
