package nebulosa.desktop.equipments

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import nebulosa.desktop.core.EventBus
import nebulosa.indi.devices.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable

@Suppress("LeakingThis")
abstract class DeviceProperty<T : Device> : SimpleObjectProperty<T>(), ChangeListener<T>, Consumer<Any>, Closeable, KoinComponent {

    protected val eventBus by inject<EventBus>()

    @JvmField val isConnected = SimpleBooleanProperty(false)
    @JvmField val isConnecting = SimpleBooleanProperty(false)

    @Volatile private var closed = false
    @Volatile private var subscriber: Disposable? = null

    init {
        addListener(this)

        subscriber = eventBus
            .filter { it is DeviceEvent<*> && it.device === value }
            .subscribe(::accept)
    }

    protected abstract fun reset()

    protected abstract fun changed(value: T)

    protected abstract fun accept(event: DeviceEvent<T>)

    final override fun changed(
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
            changed(newValue)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun accept(event: Any) {
        if (closed) return

        when (event) {
            is DeviceConnected,
            is DeviceDisconnected -> Platform.runLater {
                isConnecting.set(false)
                isConnected.set(value.isConnected)
            }
            is DeviceIsConnecting -> Platform.runLater { isConnecting.set(true) }
            else -> accept(event as DeviceEvent<T>)
        }
    }

    override fun close() {
        closed = true
        subscriber?.dispose()
        subscriber = null
    }
}
