package nebulosa.xisf

import nebulosa.image.format.Hdu
import nebulosa.image.format.ImageFormat
import nebulosa.image.format.ImageHdu
import nebulosa.io.SeekableSource
import okio.Buffer
import okio.Sink

/**
 * Extensible Image Serialization Format (XISF) is the native file format of PixInsight.
 * It is a free, open format for storage, management and interchange of digital images
 * and associated data.
 *
 * @see <a href="https://pixinsight.com/doc/docs/XISF-1.0-spec/XISF-1.0-spec.html">XISF Version 1.0 Specification</a>
 */
data object XisfFormat : ImageFormat {

    override fun read(source: SeekableSource): List<ImageHdu> {
        val buffer = Buffer()

        source.read(buffer, 8) // XISF0100
        buffer.readString(Charsets.US_ASCII)

        // Header length (4) + reserved (4)
        source.read(buffer, 4 + 4)
        val headerLength = buffer.readIntLe().toLong()
        buffer.skip(4) // reserved

        // XISF Header.
        source.read(buffer, headerLength)
        val stream = XisfHeaderInputStream(buffer.inputStream())
        val hdus = ArrayList<XisfMonolithicFileHeaderImageHdu>(2)

        while (true) {
            val header = stream.read() ?: break

            when (header) {
                is XisfMonolithicFileHeader.Image -> hdus.add(XisfMonolithicFileHeaderImageHdu(header, source))
            }
        }

        return hdus
    }

    override fun write(sink: Sink, hdus: Iterable<Hdu<*>>) {
        TODO("Not implemented yet")
    }
}
