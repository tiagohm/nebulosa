package nebulosa.grpc.driver

import nebulosa.grpc.EventSender
import nebulosa.grpc.PropertyPermission.READ_WRITE
import nebulosa.grpc.PropertyState.IDLE
import nebulosa.grpc.SwitchRule.AT_MOST_ONE
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractDriver : Driver {

    private val senders = HashSet<EventSender>()
    private val properties = ConcurrentHashMap<String, Property<*>>()

    private val connection = SwitchProperty("CONNECTION", "Connection", "Main Control", RW, AT_MOST_ONE, IDLE).apply {
        elements.add(SwitchElement("CONNECT", "Connect", false))
        elements.add(SwitchElement("DISCONNECT", "Connect", true))
    }

    init {
        registerProperty(connection)
    }

    protected open fun connect() = false

    protected open fun disconnect() = false

    final override fun attach(sender: EventSender) {
        senders.add(sender)
    }

    final override fun detach(sender: EventSender) {
        senders.remove(sender)
    }

    final override fun ask(name: String) {
        if (name.isEmpty()) {
            val event = properties[name]?.makeDefPropertyEvent(this.name) ?: return
            senders.forEach { it.sendEvent(event) }
        } else {
            for (property in properties) {
                val event = property.value.makeDefPropertyEvent(this.name)
                senders.forEach { it.sendEvent(event) }
            }
        }
    }

    protected fun registerProperty(property: Property<*>) {
        if (!properties.containsKey(property.name)) {
            properties[property.name] = property
        }
    }

    protected fun unregisterProperty(property: Property<*>) {
        properties.remove(property.name) ?: return
    }
}
