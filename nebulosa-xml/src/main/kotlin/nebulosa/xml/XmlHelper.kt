package nebulosa.xml

import javax.xml.stream.XMLStreamReader

fun XMLStreamReader.attribute(name: String): String? {
    for (i in 0 until attributeCount) {
        if (getAttributeLocalName(i) == name) {
            return getAttributeValue(i)
        }
    }

    return null
}
