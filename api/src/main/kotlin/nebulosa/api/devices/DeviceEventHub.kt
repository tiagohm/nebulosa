package nebulosa.api.devices

import io.reactivex.rxjava3.subjects.PublishSubject
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.log.loggerFor
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

abstract class DeviceEventHub<D : Device, E : DeviceEvent<D>>(eventName: String) : Closeable {

    private val throttler = PublishSubject.create<E>()
    private val listenable = ConcurrentHashMap<D, Long>(2)

    private val updateEventName = "$eventName.UPDATED"
    private val attachedEventName = "$eventName.ATTACHED"
    private val detachedEventName = "$eventName.DETACHED"

    init {
        throttler
            .throttleLast(1000, TimeUnit.MILLISECONDS)
            .subscribe {
                val device = it.device ?: return@subscribe
                val lastTime = listenable[device] ?: return@subscribe
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastTime < SEND_UPDATE_INTERVAL) {
                    sendUpdate(device)
                } else {
                    listenable.remove(device)
                    LOG.warn("device {} ({}) is no longer listenable", device.name, device.id)
                }
            }
    }

    abstract fun sendMessage(eventName: String, device: D)

    open fun sendUpdate(device: D) = sendMessage(updateEventName, device)

    open fun onAttached(device: D) = sendMessage(attachedEventName, device)

    open fun onDetached(device: D) = sendMessage(detachedEventName, device)

    open fun onConnectionChanged(device: D) = sendUpdate(device)

    protected fun onNext(event: E) = throttler.onNext(event)

    fun listen(device: D) = listenable.put(device, System.currentTimeMillis()) != null

    override fun close() = throttler.onComplete()

    companion object {

        const val SEND_UPDATE_INTERVAL = 60000 // 1 min.

        @JvmStatic private val LOG = loggerFor<DeviceEventHub<*, *>>()
    }
}
