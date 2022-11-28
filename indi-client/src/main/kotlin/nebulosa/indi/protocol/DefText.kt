package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("defText")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [DefText::class])
class DefText : DefElement<String>(), TextElement {

    override var value = ""
}
