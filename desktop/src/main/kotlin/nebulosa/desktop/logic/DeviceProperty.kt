package nebulosa.desktop.logic

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.core.EventBus.Companion.observeOnFXThread
import nebulosa.indi.device.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable

@Suppress("LeakingThis")
abstract class DeviceProperty<T : Device> : SimpleObjectProperty<T>(), Closeable, KoinComponent {

    protected val eventBus by inject<EventBus>()

    @JvmField val isConnected = SimpleBooleanProperty(false)
    @JvmField val isConnecting = SimpleBooleanProperty(false)

    private val subscribers = arrayOfNulls<Disposable>(1)

    @Volatile private var closed = false

    init {
        addListener(::onChanged)

        subscribers[0] = eventBus
            .filterIsInstance<DeviceEvent<*>> { it.device === value }
            .observeOnFXThread()
            .subscribe(::onDeviceEvent)
    }

    final override fun getName() = value?.name ?: ""

    protected abstract fun reset()

    protected abstract fun onChanged(prev: T?, new: T)

    protected fun onChanged(
        observable: ObservableValue<out T>,
        oldValue: T?, newValue: T?,
    ) {
        if (closed) return

        if (newValue == null) {
            isConnected.set(false)
            isConnecting.set(false)
            reset()
        } else {
            isConnected.set(newValue.isConnected)
            onChanged(oldValue, newValue)
        }
    }

    protected open fun onDeviceEvent(event: DeviceEvent<*>) {
        if (closed) return

        when (event) {
            is DeviceConnected -> {
                isConnected.set(true)
                isConnecting.set(false)
            }
            is DeviceDisconnected -> {
                isConnected.set(false)
                isConnecting.set(false)
            }
            is DeviceIsConnecting -> {
                isConnecting.set(true)
            }
        }
    }

    override fun close() {
        if (closed) return

        closed = true

        subscribers.forEach { it?.dispose() }
        subscribers.fill(null)
    }
}
