package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute

@XStreamAlias("getProperties")
class GetProperties : INDIProtocol() {

    @XStreamAsAttribute
    @JvmField
    val version = "1.7"
}
