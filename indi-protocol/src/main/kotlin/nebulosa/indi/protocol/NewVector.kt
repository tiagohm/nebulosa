package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamImplicit

sealed class NewVector<E : OneElement<*>> : INDIProtocol(), Vector<E> {

    @XStreamImplicit
    override var elements = ArrayList<E>(0)

    @XStreamAsAttribute
    override var state = PropertyState.IDLE

    @XStreamAsAttribute
    @JvmField
    var timeout = ""

    override fun get(name: String) = elements.firstOrNull { it.name == name }

    override fun toString() =
        "${this::class.simpleName}(device=$device, name=$name, state=$state, message=$message, timeout=$timeout, elements=$elements)"
}
