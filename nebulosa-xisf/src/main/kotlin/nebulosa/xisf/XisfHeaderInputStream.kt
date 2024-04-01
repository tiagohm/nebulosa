package nebulosa.xisf

import com.fasterxml.aalto.stax.InputFactoryImpl
import nebulosa.image.format.HeaderCard
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
        val imageType = reader.attribute("imageType")?.let { ImageType.parse(it) }
        val compression = reader.attribute("compression")?.let { CompressionFormat.parse(it) }
        val bounds = reader.attribute("bounds")?.split(":")?.let { it[0].toFloat()..it[1].toFloat() }
        val (keywords, thumbnail) = parseKeywords()

        return Image(
            width.toInt(), height.toInt(), numberOfChannels.toInt(),
            position.toLong(), size.toLong(),
            sampleFormat, colorSpace ?: ColorSpace.GRAY,
            pixelStorage ?: PixelStorageModel.PLANAR,
            byteOrder ?: ByteOrder.LITTLE,
            compression, imageType ?: ImageType.LIGHT,
            bounds ?: XisfMonolithicFileHeader.DEFAULT_BOUNDS,
            keywords, thumbnail,
        )
    }

    private fun parseKeywords(): Pair<Collection<HeaderCard>, Image?> {
        val name = reader.localName

        val cards = ArrayList<HeaderCard>()
        var thumbnail: Image? = null

        fun addHeaderCard(card: HeaderCard) {
            if (cards.find { it.key == card.key } == null) {
                cards.add(card)
            }
        }

        while (reader.hasNext()) {
            val type = reader.next()

            if (type == XMLStreamConstants.END_ELEMENT && reader.localName == name) {
                break
            } else if (type == XMLStreamConstants.START_ELEMENT) {
                when (reader.localName) {
                    "FITSKeyword" -> addHeaderCard(parseFITSKeyword())
                    "Thumbnail" -> thumbnail = parseImage()
                    "Property" -> addHeaderCard(parseProperty() ?: continue)
                }
            }
        }

        return cards to thumbnail
    }

    private fun parseFITSKeyword(): HeaderCard {
        val name = reader.attribute("name") ?: ""
        val value = reader.attribute("value")?.trim() ?: ""
        val comment = reader.attribute("comment") ?: ""
        val type = XisfPropertyType.fromValue(value)
        return XisfHeaderCard(name, value.trim('\'').trim(), comment, type)
    }

    private fun parseProperty(): HeaderCard? {
        val id = reader.attribute("id")!!
        val key = AstronomicalImageProperties[id] ?: return null
        val propertyType = XisfPropertyType.fromTypeName(reader.attribute("type")!!) ?: return null
        val value = reader.attribute("value") ?: reader.elementText.trim()
        return XisfHeaderCard(key.key, value, key.comment, propertyType)
    }

    override fun close() {
        reader.close()
    }

    companion object {

        @JvmStatic private val XML_INPUT_FACTORY = InputFactoryImpl()
    }
}
