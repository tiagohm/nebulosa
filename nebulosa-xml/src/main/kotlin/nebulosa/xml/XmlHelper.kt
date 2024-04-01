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

private val XML_ESCAPE_CHARS = charArrayOf('"', '&', '<', '>')
private val XML_ESCAPE_CHAR_CODES = arrayOf("&quot;", "&amp;", "&lt;", "&gt;")

fun String.escapeXml(): String {
    if (none { it in XML_ESCAPE_CHARS }) return this

    return buildString(length) {
        for (c in this) {
            val index = XML_ESCAPE_CHARS.indexOf(c)

            if (index >= 0) {
                append(XML_ESCAPE_CHAR_CODES[index])
            } else {
                append(c)
            }
        }
    }
}
