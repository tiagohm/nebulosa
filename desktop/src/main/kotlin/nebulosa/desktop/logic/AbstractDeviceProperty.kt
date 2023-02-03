package nebulosa.desktop.logic

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import nebulosa.indi.device.*
import org.koin.core.component.KoinComponent

@Suppress("LeakingThis", "UNCHECKED_CAST")
abstract class AbstractDeviceProperty<D : Device> : SimpleObjectProperty<D>(), DeviceProperty<D>, KoinComponent {

    override val connectedProperty = SimpleBooleanProperty(false)
    override val connectingProperty = SimpleBooleanProperty(false)

    private val subscribers = arrayOfNulls<Disposable>(1)
    private val listeners = hashSetOf<DevicePropertyListener<D>>()

    @Volatile private var closed = false

    init {
        addListener(::onChanged)

        subscribers[0] = EventBus.DEVICE
            .subscribe(filter = { it.device === value }, observeOnJavaFX = true, next = ::onDeviceEvent)
    }

    override fun registerListener(listener: DevicePropertyListener<D>) {
        listeners.add(listener)
    }

    override fun unregisterListener(listener: DevicePropertyListener<D>) {
        listeners.remove(listener)
    }

    final override fun getName() = value?.name ?: ""

    override fun onChanged(prev: D?, device: D) = Unit

    override fun onDeviceEvent(event: DeviceEvent<*>, device: D) = Unit

    protected fun onChanged(
        observable: ObservableValue<out D>,
        oldValue: D?, newValue: D?,
    ) {
        if (closed) return

        if (newValue == null) {
            connectedProperty.set(false)
            connectingProperty.set(false)

            onReset()

            listeners.forEach { it.onReset() }
        } else {
            connectedProperty.set(newValue.connected)

            onChanged(oldValue, newValue)

            listeners.forEach { it.onChanged(oldValue, newValue) }
        }
    }

    protected fun onDeviceEvent(event: DeviceEvent<*>) {
        if (closed) return

        when (event) {
            is DeviceConnected -> {
                connectedProperty.set(true)
                connectingProperty.set(false)
            }
            is DeviceDisconnected -> {
                connectedProperty.set(false)
                connectingProperty.set(false)
            }
            is DeviceIsConnecting -> {
                connectingProperty.set(true)
            }
        }

        val device = event.device as D

        onDeviceEvent(event, device)

        listeners.forEach { it.onDeviceEvent(event, device) }
    }

    override fun close() {
        if (closed) return

        closed = true

        subscribers.forEach { it?.dispose() }
        subscribers.fill(null)

        listeners.clear()
    }
}
