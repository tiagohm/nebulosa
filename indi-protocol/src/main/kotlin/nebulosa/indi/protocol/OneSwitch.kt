package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamConverter
import nebulosa.indi.protocol.xml.ToAttributedValueConverter

@XStreamAlias("oneSwitch")
@XStreamConverter(value = ToAttributedValueConverter::class, strings = ["value"], types = [OneSwitch::class])
class OneSwitch : OneElement<SwitchState>(), SwitchElement {

    override var value = SwitchState.OFF
}
