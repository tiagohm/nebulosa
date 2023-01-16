package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAsAttribute

sealed class DefElement<T> : INDIProtocol(), Element<T> {

    @XStreamAsAttribute
    @JvmField
    var label = ""

    override fun toString() = "${this::class.simpleName}(name=$name, label=$label, message=$message, value=$value)"
}
