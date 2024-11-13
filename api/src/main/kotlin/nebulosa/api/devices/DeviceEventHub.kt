package nebulosa.api.devices

import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceType
import nebulosa.log.loggerFor
import nebulosa.log.w
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.concurrent.timer

abstract class DeviceEventHub<D : Device, E : DeviceEvent<D>>(deviceType: DeviceType) : Consumer<E>, AutoCloseable {

    private val throttlers = ConcurrentHashMap<D, Throttler>(4)
    private val listenable = ConcurrentHashMap<D, Long>(2)

    private val updateEventName = "$deviceType.UPDATED"
    private val attachedEventName = "$deviceType.ATTACHED"
    private val detachedEventName = "$deviceType.DETACHED"

    protected abstract fun sendMessage(eventName: String, device: D)

    open fun sendUpdate(device: D) {
        sendMessage(updateEventName, device)
    }

    open fun onAttached(device: D) {
        throttlers.computeIfAbsent(device, ::Throttler)
        sendMessage(attachedEventName, device)
    }

    open fun onDetached(device: D) {
        sendMessage(detachedEventName, device)
        throttlers.remove(device)?.close()
    }

    open fun onConnectionChanged(device: D) {
        sendUpdate(device)
    }

    protected open fun onNext(event: E) {
        val device = event.device ?: return
        throttlers[device]?.accept(event)
    }

    fun listen(device: D) {
        listenable.put(device, System.currentTimeMillis())
    }

    override fun accept(event: E) {
        val device = event.device ?: return
        val lastTime = listenable[device] ?: return
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTime < SEND_UPDATE_INTERVAL) {
            sendUpdate(device)
        } else {
            listenable.remove(device)
            LOG.w("device {} ({}) is no longer listenable", device.name, device.id)
        }
    }

    override fun close() {
        throttlers.values.forEach(AutoCloseable::close)
        throttlers.clear()
    }

    private inner class Throttler(
        device: Device,
        private val period: Long = 1000, // 1s
    ) : Consumer<E>, AutoCloseable {

        private val store = AtomicReference<E>()

        private val timer = timer("${device.name} Throttler Timer", true, period = period) {
            store.getAndSet(null)?.also(this@DeviceEventHub::accept)
        }

        override fun accept(event: E) {
            store.set(event)
        }

        override fun close() {
            store.set(null)
            timer.cancel()
        }
    }

    companion object {

        const val SEND_UPDATE_INTERVAL = 60000 // 1 min.

        private val LOG = loggerFor<DeviceEventHub<*, *>>()
    }
}
