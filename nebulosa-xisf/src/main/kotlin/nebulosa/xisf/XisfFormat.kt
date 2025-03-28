package nebulosa.xisf

import nebulosa.fits.Bitpix
import nebulosa.fits.ValueType
import nebulosa.fits.bitpix
import nebulosa.fits.frame
import nebulosa.image.format.Hdu
import nebulosa.image.format.ImageFormat
import nebulosa.image.format.ImageHdu
import nebulosa.image.format.ImageModifier
import nebulosa.io.AbstractSeekableSource
import nebulosa.io.ByteOrder
import nebulosa.io.SeekableSource
import nebulosa.io.readDouble
import nebulosa.io.readFloat
import nebulosa.io.readInt
import nebulosa.io.readShort
import nebulosa.io.sink
import nebulosa.io.transferFully
import nebulosa.io.writeDoubleLe
import nebulosa.io.writeFloatLe
import nebulosa.xisf.XisfMonolithicFileHeader.*
import nebulosa.xml.escapeXml
import okio.Buffer
import okio.BufferedSource
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

    const val SIGNATURE = "XISF0100"

    override fun read(source: SeekableSource): List<ImageHdu> {
        return Buffer().use { buffer ->
            source.read(buffer, 8)
            check(buffer.readSignature() == SIGNATURE) { "invalid signature" }

            // Header length (4) + reserved (4)
            source.read(buffer, 8)
            val headerLength = buffer.readIntLe()
            // buffer.skip(4) // reserved
            buffer.clear()

            // XISF Header.
            val headerData = ByteArray(headerLength)
            headerData.sink().use { buffer.transferFully(source, it, headerLength.toLong()) }
            val stream = XisfHeaderInputStream(ByteArrayInputStream(headerData))
            val hdus = ArrayList<XisfMonolithicFileHeaderImageHdu>(2)

            while (true) {
                val header = stream.read() ?: break

                when (header) {
                    is Image -> hdus.add(XisfMonolithicFileHeaderImageHdu(header, source))
                }
            }

            hdus
        }
    }

    override fun write(sink: Sink, hdus: Iterable<Hdu<*>>, modifier: ImageModifier) {
        val bitpix = modifier.bitpix()

        Buffer().use { buffer ->
            val headerSize = writeHeader(buffer, hdus, bitpix)
            val byteCount = buffer.readAll(sink)

            val remainingBytes = headerSize - byteCount
            check(remainingBytes >= 0L) { "unexpected remaining bytes: $remainingBytes" }
            buffer.write(ZeroSource, remainingBytes)
            buffer.readAll(sink)

            for (hdu in hdus) {
                if (hdu is ImageHdu) {
                    val sampleFormat = SampleFormat.from(bitpix ?: hdu.header.bitpix)

                    if (hdu.isMono) {
                        hdu.data.red.writeTo(buffer, sink, sampleFormat)
                    } else {
                        hdu.data.red.writeTo(buffer, sink, sampleFormat)
                        hdu.data.green.writeTo(buffer, sink, sampleFormat)
                        hdu.data.blue.writeTo(buffer, sink, sampleFormat)
                    }
                }
            }
        }
    }

    private fun writeHeader(buffer: Buffer, hdus: Iterable<Hdu<*>>, bitpix: Bitpix? = null, initialHeaderSize: Int = 4096): Int {
        buffer.clear()

        buffer.writeString(MAGIC_HEADER, Charsets.US_ASCII)
        buffer.writeLongLe(0L)
        buffer.writeUtf8(XML_VERSION)
        buffer.writeUtf8(XML_COMMENT)
        buffer.writeUtf8(XISF_START_TAG)

        for (hdu in hdus) {
            if (hdu is ImageHdu) {
                val header = hdu.header
                val colorSpace = if (hdu.isMono) ColorSpace.GRAY else ColorSpace.RGB
                val imageType = ImageType.parse(header.frame ?: "Light") ?: ImageType.LIGHT
                val sampleFormat = SampleFormat.from(bitpix ?: hdu.header.bitpix)
                val imageSize = hdu.width * hdu.height * hdu.numberOfChannels * sampleFormat.byteLength
                val extra = if (sampleFormat.bitpix.code < 0) " bounds=\"0:1\"" else ""

                IMAGE_START_TAG
                    .format(
                        hdu.width, hdu.height, hdu.numberOfChannels,
                        sampleFormat.code, colorSpace.code, imageType.code,
                        initialHeaderSize, imageSize, extra
                    ).also(buffer::writeUtf8)

                for ((name, key) in AstronomicalImageProperties) {
                    if (key != null) {
                        if (key.valueType == ValueType.STRING || key.valueType == ValueType.ANY) {
                            val value = header.getStringOrNull(key) ?: continue
                            buffer.writeUtf8(STRING_PROPERTY_TAG.format(name, value.escapeXml()))
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
                    val value = if (keyword.key == sampleFormat.bitpix.key) sampleFormat.bitpix.value
                    else if (keyword.isStringType) "'${keyword.value.escapeXml()}'" else keyword.value
                    buffer.writeUtf8(FITS_KEYWORD_TAG.format(keyword.key, value, keyword.comment.escapeXml()))
                }
            }
        }

        buffer.writeUtf8(IMAGE_END_TAG)
        buffer.writeUtf8(XISF_END_TAG)

        val size = buffer.size
        val remainingBytes = initialHeaderSize - size

        if (remainingBytes < 0) {
            return writeHeader(buffer, hdus, bitpix, initialHeaderSize * 2)
        }

        val headerSize = size - 16

        buffer.readAndWriteUnsafe().use {
            it.seek(8L)
            it.data!![it.start + 0] = (headerSize and 0xFF).toByte()
            it.data!![it.start + 1] = (headerSize shr 8 and 0xFF).toByte()
            it.data!![it.start + 2] = (headerSize shr 16 and 0xFF).toByte()
            it.data!![it.start + 3] = (headerSize shr 24 and 0xFF).toByte()
        }

        return initialHeaderSize
    }

    @JvmStatic
    fun BufferedSource.readSignature() = if (request(8L)) readString(8L, Charsets.US_ASCII) else ""

    @JvmStatic
    internal fun Buffer.readPixel(format: SampleFormat, byteOrder: ByteOrder): Float {
        return when (format) {
            SampleFormat.UINT8 -> (readByte().toInt() and 0xFF) / 255f
            SampleFormat.UINT16 -> (readShort(byteOrder).toInt() and 0xFFFF) / 65535f
            SampleFormat.UINT32 -> ((readInt(byteOrder).toLong() and 0xFFFFFFFF) / 4294967295.0).toFloat()
            SampleFormat.UINT64 -> TODO("Unsupported UInt64 sample format")
            SampleFormat.FLOAT32 -> readFloat(byteOrder)
            SampleFormat.FLOAT64 -> readDouble(byteOrder).toFloat()
        }
    }

    @JvmStatic
    internal fun FloatArray.writeTo(buffer: Buffer, sink: Sink, format: SampleFormat) {
        var idx = 0

        while (idx < size) {
            repeat(min(256, size - idx)) { buffer.writePixel(this[idx++], format) }
            buffer.readAll(sink)
        }

        buffer.readAll(sink)
    }

    @JvmStatic
    internal fun Buffer.writePixel(pixel: Float, format: SampleFormat) {
        when (format) {
            SampleFormat.UINT8 -> writeByte((pixel * 255f).toInt())
            SampleFormat.UINT16 -> writeShortLe((pixel * 65535f).toInt())
            SampleFormat.UINT32 -> writeIntLe(((pixel * 4294967295.0).toLong() and 0xFFFFFFFF).toInt())
            SampleFormat.UINT64 -> TODO("Unsupported UInt64 sample format")
            SampleFormat.FLOAT32 -> writeFloatLe(pixel)
            SampleFormat.FLOAT64 -> writeDoubleLe(pixel.toDouble())
        }
    }

    private const val MAGIC_HEADER = "XISF0100"
    private const val XML_VERSION = """<?xml version="1.0"?>"""
    private const val XML_COMMENT =
        """<!-- Extensible Image Serialization Format - XISF version 1.0 Created with Nebulosa - https://github.com/tiagohm/nebulosa -->"""
    private const val XISF_START_TAG =
        """<xisf version="1.0" xmlns="http://www.pixinsight.com/xisf" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.pixinsight.com/xisf http://pixinsight.com/xisf/xisf-1.0.xsd">"""
    private const val XISF_END_TAG = "</xisf>"
    private const val IMAGE_START_TAG =
        """<Image geometry="%d:%d:%d" sampleFormat="%s" colorSpace="%s" imageType="%s" pixelStorage="Planar" location="attachment:%d:%d"%s>"""
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
