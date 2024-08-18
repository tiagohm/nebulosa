package nebulosa.api.devices

import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.log.loggerFor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

abstract class DeviceEventHub<D : Device, E : DeviceEvent<D>>(eventName: String) : Consumer<E>, AutoCloseable {

    private val throttlers = HashMap<D, Throttler>()
    private val listenable = ConcurrentHashMap<D, Long>(2)

    private val updateEventName = "$eventName.UPDATED"
    private val attachedEventName = "$eventName.ATTACHED"
    private val detachedEventName = "$eventName.DETACHED"

    abstract fun sendMessage(eventName: String, device: D)

    open fun sendUpdate(device: D) {
        sendMessage(updateEventName, device)
    }

    open fun onAttached(device: D) {
        sendMessage(attachedEventName, device)
    }

    open fun onDetached(device: D) {
        sendMessage(detachedEventName, device)
    }

    open fun onConnectionChanged(device: D) {
        sendUpdate(device)
    }

    fun onDeviceDetached(device: D) {
        throttlers.remove(device)?.onComplete()
    }

    protected fun onNext(event: E) {
        val device = event.device ?: return
        throttlers.getOrPut(device, ::Throttler).onNext(event)
    }

    fun listen(device: D): Boolean {
        return listenable.put(device, System.currentTimeMillis()) != null
    }

    override fun accept(event: E) {
        val device = event.device ?: return
        val lastTime = listenable[device] ?: return
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastTime < SEND_UPDATE_INTERVAL) {
            sendUpdate(device)
        } else {
            listenable.remove(device)
            LOG.warn("device {} ({}) is no longer listenable", device.name, device.id)
        }
    }

    override fun close() {
        throttlers.values.forEach { it.onComplete() }
        throttlers.clear()
    }

    private inner class Throttler {

        private val subject = PublishSubject.create<E>()

        init {
            subject
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .subscribe(this@DeviceEventHub)
        }

        fun onNext(event: E) = subject.onNext(event)

        fun onComplete() = subject.onComplete()
    }

    companion object {

        const val SEND_UPDATE_INTERVAL = 60000 // 1 min.

        @JvmStatic private val LOG = loggerFor<DeviceEventHub<*, *>>()
    }
}
