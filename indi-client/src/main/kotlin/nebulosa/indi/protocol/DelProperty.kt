package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("delProperty")
class DelProperty : INDIProtocol() {

    override fun toString() = "DelProperty(device=$device, name=$name, message=$message)"
}
