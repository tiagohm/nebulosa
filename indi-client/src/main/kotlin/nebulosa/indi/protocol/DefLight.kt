package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.io.ToAttributedValueConverter

@XStreamAlias("defLight")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [DefLight::class])
class DefLight : DefElement<PropertyState>(), LightElement {

    override var value = PropertyState.IDLE
}
