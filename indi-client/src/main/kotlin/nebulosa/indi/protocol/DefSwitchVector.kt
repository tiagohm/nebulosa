package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("defSwitchVector")
class DefSwitchVector : DefVector<DefSwitch>(), SwitchVector<DefSwitch> {

    @XStreamAsAttribute
    @JvmField
    var rule = SwitchRule.ANY_OF_MANY
}
