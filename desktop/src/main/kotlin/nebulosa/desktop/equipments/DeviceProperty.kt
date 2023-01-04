package nebulosa.desktop.equipments

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

@Suppress("LeakingThis")
abstract class DeviceProperty<T : Device> : SimpleObjectProperty<T>(), ChangeListener<T>, Consumer<Any>, KoinComponent {

    protected val eventBus by inject<EventBus>()

    @JvmField val isConnected = SimpleBooleanProperty(false)
    @JvmField val isConnecting = SimpleBooleanProperty(false)

    init {
        addListener(this)

        eventBus
            .filter { it is DeviceEvent<*> }
            .subscribe(this)
    }

    protected abstract fun reset()

    protected abstract fun changed(value: T)

    protected abstract fun accept(event: DeviceEvent<*>)

    final override fun changed(observable: ObservableValue<out T>, oldValue: T?, newValue: T?) {
        if (newValue == null) {
            isConnected.set(false)
            isConnecting.set(false)
            reset()
        } else {
            isConnected.set(newValue.isConnected)
            isConnecting.set(newValue.isConnecting)
            changed(newValue)
        }
    }

    final override fun accept(event: Any) {
        if (event is DeviceEvent<*> && event.device === value) {
            when (event) {
                is DeviceConnected,
                is DeviceDisconnected -> Platform.runLater {
                    isConnecting.set(false)
                    isConnected.set(value.isConnected)
                }
                is DeviceIsConnecting -> Platform.runLater { isConnecting.set(true) }
                else -> accept(event)
            }
        }
    }
}
