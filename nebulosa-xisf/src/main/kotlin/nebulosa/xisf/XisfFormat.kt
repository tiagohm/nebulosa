package nebulosa.xisf

import nebulosa.fits.ValueType
import nebulosa.fits.frame
import nebulosa.image.format.Hdu
import nebulosa.image.format.ImageFormat
import nebulosa.image.format.ImageHdu
import nebulosa.io.*
import nebulosa.xisf.XisfMonolithicFileHeader.ColorSpace
import nebulosa.xisf.XisfMonolithicFileHeader.ImageType
import okio.Buffer
import okio.Sink
import okio.Timeout
import java.io.ByteArrayInputStream
import kotlin.math.min

/**
 * Extensible Image Serialization Format (XISF) is the native file format of PixInsight.
 * It is a free, open format for storage, management and interchange of digital images
 * and associated data.
 *
 * @see <a href="https://pixinsight.com/doc/docs/XISF-1.0-spec/XISF-1.0-spec.html">XISF Version 1.0 Specification</a>
 */
data object XisfFormat : ImageFormat {

    override fun read(source: SeekableSource): List<ImageHdu> {
        return Buffer().use { buffer ->
            source.read(buffer, 8) // XISF0100
            check(buffer.readString(Charsets.US_ASCII) == "XISF0100") { "invalid magic bytes" }

            // Header length (4) + reserved (4)
            source.read(buffer, 8)
            val headerLength = buffer.readIntLe()
            // buffer.skip(4) // reserved
            buffer.clear()

            // XISF Header.
            val headerData = ByteArray(headerLength)
            val headerSink = headerData.sink()
            buffer.transferFully(source, headerSink, headerLength.toLong())
            val stream = XisfHeaderInputStream(ByteArrayInputStream(headerData))
            val hdus = ArrayList<XisfMonolithicFileHeaderImageHdu>(2)

            while (true) {
                val header = stream.read() ?: break

                when (header) {
                    is XisfMonolithicFileHeader.Image -> hdus.add(XisfMonolithicFileHeaderImageHdu(header, source))
                }
            }

            hdus
        }
    }

    override fun write(sink: Sink, hdus: Iterable<Hdu<*>>) {
        Buffer().use { buffer ->
            buffer.writeString(MAGIC_HEADER, Charsets.US_ASCII)
            buffer.writeLongLe(0L)
            buffer.writeUtf8(XML_VERSION)
            buffer.writeUtf8(XML_COMMENT)
            buffer.writeUtf8(XISF_START_TAG)

            for (hdu in hdus) {
                if (hdu is ImageHdu) {
                    val header = hdu.header
                    val colorSpace = if (hdu.isMono) ColorSpace.GRAY else ColorSpace.RGB
                    val imageType = ImageType.valueOf(header.frame ?: "LIGHT")
                    val imageSize = hdu.width * hdu.height * hdu.numberOfChannels * 4

                    IMAGE_START_TAG
                        .format(hdu.width, hdu.height, hdu.numberOfChannels, colorSpace.code, imageType.code, MAX_HEADER_SIZE, imageSize)
                        .also(buffer::writeUtf8)

                    for ((name, key) in AstronomicalImageProperties) {
                        if (key != null) {
                            if (key.valueType == ValueType.STRING || key.valueType == ValueType.ANY) {
                                val value = header.getStringOrNull(key) ?: continue
                                buffer.writeUtf8(STRING_PROPERTY_TAG.format(name, value))
                            } else if (key.valueType == ValueType.LOGICAL) {
                                val value = header.getBooleanOrNull(key) ?: continue
                                buffer.writeUtf8(NON_STRING_PROPERTY_TAG.format(name, XisfPropertyType.BOOLEAN.typeName, if (value) 1 else 0))
                            } else if (key.valueType == ValueType.INTEGER) {
                                val value = header.getLongOrNull(key) ?: continue
                                buffer.writeUtf8(NON_STRING_PROPERTY_TAG.format(name, XisfPropertyType.INT64.typeName, value))
                            } else if (key.valueType == ValueType.REAL) {
                                val value = header.getDoubleOrNull(key) ?: continue
                                buffer.writeUtf8(NON_STRING_PROPERTY_TAG.format(name, XisfPropertyType.FLOAT64.typeName, value))
                            }
                        }
                    }

                    for (keyword in header) {
                        buffer.writeUtf8(FITS_KEYWORD_TAG.format(keyword.key, keyword.value, keyword.comment))
                    }
                }
            }

            buffer.writeUtf8(IMAGE_END_TAG)
            buffer.writeUtf8(XISF_END_TAG)

            val headerSize = buffer.size - 8

            buffer.readAndWriteUnsafe().use {
                it.seek(8L)
                it.data!![it.start + 0] = (headerSize and 0xFF).toByte()
                it.data!![it.start + 1] = (headerSize shr 8 and 0xFF).toByte()
                it.data!![it.start + 2] = (headerSize shr 16 and 0xFF).toByte()
                it.data!![it.start + 3] = (headerSize shr 24 and 0xFF).toByte()
            }

            val byteCount = buffer.readAll(sink)

            val remainingBytes = MAX_HEADER_SIZE - byteCount
            check(remainingBytes >= 0L) { "unexpected remaining bytes: $remainingBytes" }
            buffer.write(ZeroSource, remainingBytes)
            buffer.readAll(sink)

            for (hdu in hdus) {
                if (hdu is ImageHdu) {
                    if (hdu.isMono) {
                        hdu.data.red.readTo(buffer, sink)
                    } else {
                        hdu.data.red.readTo(buffer, sink)
                        hdu.data.green.readTo(buffer, sink)
                        hdu.data.blue.readTo(buffer, sink)
                    }
                }
            }
        }
    }

    private fun FloatArray.readTo(buffer: Buffer, sink: Sink) {
        var idx = 0

        while (idx < size) {
            repeat(min(256, size - idx)) { buffer.writeFloatLe(this[idx++]) }
            buffer.readAll(sink)
        }

        buffer.readAll(sink)
    }

    private const val MAGIC_HEADER = "XISF0100"
    private const val MAX_HEADER_SIZE = 4096
    private const val XML_VERSION = """<?xml version="1.0"?>"""
    private const val XML_COMMENT =
        """<!-- Extensible Image Serialization Format - XISF version 1.0 Created with Nebulosa - https://github.com/tiagohm/nebulosa -->"""
    private const val XISF_START_TAG =
        """<xisf version="1.0" xmlns="http://www.pixinsight.com/xisf" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.pixinsight.com/xisf http://pixinsight.com/xisf/xisf-1.0.xsd">"""
    private const val XISF_END_TAG = "</xisf>"
    private const val IMAGE_START_TAG =
        """<Image geometry="%d:%d:%d" sampleFormat="Float32" colorSpace="%s" imageType="%s" pixelStorage="Planar" location="attachment:%d:%d">"""
    private const val IMAGE_END_TAG = "</Image>"
    private const val STRING_PROPERTY_TAG = """<Property id="%s" type="String">%s</Property>"""
    private const val NON_STRING_PROPERTY_TAG = """<Property id="%s" type="%s" value="%s" />"""
    private const val FITS_KEYWORD_TAG = """<FITSKeyword name="%s" value="%s" comment="%s"/>"""

    private data object ZeroSource : AbstractSeekableSource() {

        override val size = Long.MAX_VALUE
        override val timeout = Timeout.NONE

        override fun transfer(output: ByteArray, start: Int, length: Int): Int {
            repeat(length) { output[start + it] = 0 }
            return length
        }
    }
}
