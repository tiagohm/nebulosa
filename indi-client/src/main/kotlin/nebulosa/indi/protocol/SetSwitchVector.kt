package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("setSwitchVector")
class SetSwitchVector : SetVector<OneSwitch>(), SwitchVector<OneSwitch> {

    @XStreamAsAttribute
    @JvmField
    var rule = SwitchRule.ANY_OF_MANY

    override fun toString() =
        "SetSwitchVector(device=$device, name=$name, state=$state, message=$message, rule=$rule, timeout=$timeout, elements=$elements)"
}
