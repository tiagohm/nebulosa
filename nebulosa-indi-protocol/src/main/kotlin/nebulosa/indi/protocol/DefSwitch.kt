package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.xml.ToAttributedValueConverter

@XStreamAlias("defSwitch")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [DefSwitch::class])
class DefSwitch : DefElement<SwitchState>(), SwitchElement {

    override var value = SwitchState.OFF
}
