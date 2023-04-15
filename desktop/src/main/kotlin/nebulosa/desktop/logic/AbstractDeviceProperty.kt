package nebulosa.desktop.logic

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import nebulosa.desktop.helper.runBlockingMain
import nebulosa.indi.device.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Suppress("UNCHECKED_CAST")
abstract class AbstractDeviceProperty<D : Device> : SimpleObjectProperty<D>(), DeviceProperty<D> {

    override val connectedProperty = SimpleBooleanProperty(false)
    override val connectingProperty = SimpleBooleanProperty(false)

    private val subscribers = arrayOfNulls<Disposable>(1)
    private val listeners = linkedSetOf<DevicePropertyListener<D>>()

    @Volatile private var closed = false

    final override fun getName() = value?.name ?: ""

    fun initialize() {
        registerListener(this)
        addListener(::onChanged)
    }

    override fun registerListener(listener: DevicePropertyListener<D>) {
        listeners.add(listener)
    }

    override fun unregisterListener(listener: DevicePropertyListener<D>) {
        listeners.remove(listener)
    }

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

            listeners.forEach { it.onReset() }
        } else {
            connectedProperty.set(newValue.connected)

            listeners.forEach { it.onChanged(oldValue, newValue) }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onDeviceEvent(event: DeviceEvent<*>) = runBlockingMain {
        if (closed || event.device !== value) return@runBlockingMain

        when (event) {
            is DeviceConnected -> {
                connectedProperty.set(true)
                connectingProperty.set(false)

                listeners.forEach { it.onDeviceConnected() }
            }
            is DeviceDisconnected -> {
                connectedProperty.set(false)
                connectingProperty.set(false)

                listeners.forEach { it.onDeviceDisconnected() }
            }
            is DeviceIsConnecting -> {
                connectingProperty.set(true)
            }
        }

        val device = event.device as D

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
