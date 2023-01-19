package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.xml.ToAttributedValueConverter

@XStreamAlias("defText")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [DefText::class])
class DefText : DefElement<String>(), TextElement {

    override var value = ""

    class Builder {

        private var name = ""
        private var label = ""
        private var value = ""

        fun name(name: String) = apply { this.name = name }

        fun label(label: String) = apply { this.label = label }

        fun value(value: String) = apply { this.value = value }

        fun build() = DefText().also {
            it.name = name
            it.label = label
            it.value = value
        }
    }
}
