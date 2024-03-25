package nebulosa.xisf

import com.fasterxml.aalto.stax.InputFactoryImpl
import nebulosa.fits.FitsHeaderCard
import nebulosa.fits.FitsHeaderCardType
import nebulosa.io.ByteOrder
import nebulosa.xisf.XisfMonolithicFileHeader.*
import nebulosa.xml.attribute
import java.io.Closeable
import java.io.InputStream
import javax.xml.stream.XMLStreamConstants

class XisfHeaderInputStream(source: InputStream) : Closeable {

    private val reader = XML_INPUT_FACTORY.createXMLStreamReader(source)

    fun read(): XisfMonolithicFileHeader? {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    return parseStartElement() ?: continue
                }
            }
        }

        return null
    }

    private fun parseStartElement(): XisfMonolithicFileHeader? {
        return when (reader.localName) {
            "Image" -> parseImage()
            else -> null
        }
    }

    private fun parseImage(): Image {
        val (width, height, numberOfChannels) = reader.attribute("geometry")!!.split(":")

        val location = reader.attribute("location")!!
        check(location.startsWith("attachment"))
        val (_, position, size) = location.split(":")

        val sampleFormat = SampleFormat.valueOf(reader.attribute("sampleFormat")!!.uppercase())
        val colorSpace = reader.attribute("colorSpace")?.uppercase()?.let(ColorSpace::valueOf)
        val pixelStorage = reader.attribute("pixelStorage")?.uppercase()?.let(PixelStorageModel::valueOf)
        val byteOrder = reader.attribute("byteOrder")?.uppercase()?.let(ByteOrder::valueOf)
        val compression = reader.attribute("compression")?.let { CompressionFormat.parse(it) }
        val (keywords, thumbnail) = parseKeywords()
        // TODO: bounds (Representable Range)
        // TODO: Convert metadata into FITS keywords?

        return Image(
            width.toInt(), height.toInt(), numberOfChannels.toInt(),
            position.toLong(), size.toLong(),
            sampleFormat, colorSpace ?: ColorSpace.GRAY,
            pixelStorage ?: PixelStorageModel.PLANAR,
            byteOrder ?: ByteOrder.LITTLE,
            compression, keywords, thumbnail,
        )
    }

    private fun parseKeywords(): Pair<List<FitsHeaderCard>, Image?> {
        val name = reader.localName

        val keywords = ArrayList<FitsHeaderCard>()
        var thumbnail: Image? = null

        while (reader.hasNext()) {
            val type = reader.next()

            if (type == XMLStreamConstants.END_ELEMENT && reader.localName == name) {
                break
            } else if (type == XMLStreamConstants.START_ELEMENT) {
                when (reader.localName) {
                    "FITSKeyword" -> keywords.add(parseFITSKeyword())
                    "Thumbnail" -> thumbnail = parseImage()
                }
            }
        }

        return keywords to thumbnail
    }

    private fun parseFITSKeyword(): FitsHeaderCard {
        val name = reader.attribute("name") ?: ""
        val value = reader.attribute("value")?.trim() ?: ""
        val trimmedValue = value.trim('\'')
        val comment = reader.attribute("comment") ?: ""
        val isStringType = value.startsWith('\'') && value.endsWith('\'')
        // TODO: Identify other types
        val type = if (isStringType) FitsHeaderCardType.TEXT else FitsHeaderCardType.NONE
        return FitsHeaderCard(name, trimmedValue, comment, type)
    }

    override fun close() {
        reader.close()
    }

    companion object {

        @JvmStatic private val XML_INPUT_FACTORY = InputFactoryImpl()
    }
}
