package nebulosa.indi.protocol.xml

internal class XmlBuilder {

    private var name = ""
    private var hasValue = false
    private val text = StringBuffer(128)

    fun name(name: String) = apply { this.name = name; text.append('<').append(name) }

    fun attr(name: String, value: String) = apply { if (value.isNotEmpty()) text.append(' ').append(name).append("=\"").append(value).append('"') }

    fun attr(name: String, value: Double) = attr(name, "$value")

    fun attr(name: String, value: Any) = attr(name, "$value")

    fun value(value: String) = apply { hasValue = true; text.append('>').append(value).append("</").append(name).append('>') }

    fun value(value: Any) = value("$value")

    fun build(): String {
        if (!hasValue) text.append("/>")
        return "$text"
    }
}
