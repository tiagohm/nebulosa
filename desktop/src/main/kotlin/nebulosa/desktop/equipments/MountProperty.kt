package nebulosa.desktop.equipments

import io.reactivex.rxjava3.functions.Consumer
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import nebulosa.desktop.core.EventBus
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceDisconnected
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.mounts.Mount
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MountProperty : SimpleObjectProperty<Mount>(), ChangeListener<Mount>, Consumer<Any>, KoinComponent {

    private val eventBus by inject<EventBus>()

    @JvmField val isConnected = SimpleBooleanProperty(false)

    init {
        addListener(this)

        eventBus.subscribe(this)
    }

    override fun changed(
        observable: ObservableValue<out Mount>,
        oldValue: Mount?, newValue: Mount?,
    ) {
        if (newValue == null) {
            reset()
        } else {
            isConnected.value = newValue.isConnected
        }
    }

    fun reset() {
        isConnected.value = false
    }

    override fun accept(event: Any) {
        if (event is DeviceEvent<*> && event.device === value) {
            Platform.runLater {
                when (event) {
                    is DeviceConnected,
                    is DeviceDisconnected -> isConnected.value = value.isConnected
                }
            }
        }
    }
}
