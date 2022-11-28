package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAsAttribute

sealed class DefElement<T> : INDIProtocol(), Element<T> {

    @XStreamAsAttribute
    @JvmField
    var label = ""
}
