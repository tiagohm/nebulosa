package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.xml.ToAttributedValueConverter

@XStreamAlias("oneLight")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [OneLight::class])
class OneLight : OneElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE
}
