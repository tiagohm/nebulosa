package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("newSwitchVector")
class NewSwitchVector : NewVector<OneSwitch>(), SwitchVector<OneSwitch> {

    @XStreamAsAttribute
    @JvmField
    var rule = SwitchRule.ANY_OF_MANY
}
