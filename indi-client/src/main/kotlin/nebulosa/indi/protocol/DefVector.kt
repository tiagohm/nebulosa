package nebulosa.indi.protocol

import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamImplicit

sealed class DefVector<E : DefElement<*>> : INDIProtocol(), Vector<E> {

    @XStreamImplicit
    override var elements = ArrayList<E>(0)

    @XStreamAsAttribute
    @JvmField
    var group = ""

    @XStreamAsAttribute
    @JvmField
    var label = ""

    @XStreamAsAttribute
    @JvmField
    var perm = PropertyPermission.RW

    @XStreamAsAttribute
    override var state = PropertyState.IDLE

    @XStreamAsAttribute
    @JvmField
    var timeout = 0.0

    override fun get(name: String) = elements.firstOrNull { it.name == name }

    override fun toString() =
        "${this::class.simpleName}(device=$device, group=$group, name=$name, label=$label, message=$message, perm=$perm, state=$state, timeout=$timeout, elements=$elements)"
}
