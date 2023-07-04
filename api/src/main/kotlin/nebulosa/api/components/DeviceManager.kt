package nebulosa.api.components

import nebulosa.indi.device.*
import java.util.*

abstract class DeviceManager<T : Device> : ArrayList<T>(2), DeviceEventHandler {

    private val handlers = Collections.synchronizedSet(HashSet<DeviceEventHandler>())

    protected abstract fun canHandleEvent(event: DeviceEvent<*>): Boolean

    protected abstract fun onDeviceEventReceived(event: DeviceEvent<T>)

    fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onEventReceived(event: DeviceEvent<*>) {
        if (canHandleEvent(event)) {
            when (event) {
                is DeviceAttached<*> -> add(event.device as T)
                is DeviceDetached<*> -> remove(event.device as T)
                else -> {
                    onDeviceEventReceived(event as DeviceEvent<T>)
                    handlers.forEach { it.onEventReceived(event) }
                }
            }
        }
    }

    operator fun contains(name: String): Boolean {
        return any { it.name == name }
    }
}
