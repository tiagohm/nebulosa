package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.xml.ToAttributedValueConverter

@XStreamAlias("oneText")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [OneText::class])
class OneText : OneElement<String>(), TextElement {

    override var value = ""
}
