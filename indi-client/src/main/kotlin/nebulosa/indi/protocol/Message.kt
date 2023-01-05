package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAlias

@XStreamAlias("message")
class Message : INDIProtocol() {

    override fun toString() = "Message(device=$device, name=$name, message=$message)"
}
