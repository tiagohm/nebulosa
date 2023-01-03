package nebulosa.desktop.equipments

import io.reactivex.rxjava3.functions.Consumer
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import nebulosa.desktop.core.eventbus.EventBus
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceDisconnected
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.devices.filterwheels.FilterWheelFilterIsMovingChanged
import nebulosa.indi.devices.filterwheels.FilterWheelPositionChanged
import nebulosa.indi.devices.filterwheels.FilterWheelSlotCountChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FilterWheelProperty : SimpleObjectProperty<FilterWheel>(), ChangeListener<FilterWheel>, Consumer<Any>, KoinComponent {

    private val eventBus by inject<EventBus>()

    @JvmField val isConnected = SimpleBooleanProperty(false)
    @JvmField val slotCount = SimpleIntegerProperty(0)
    @JvmField val position = SimpleIntegerProperty(-1)
    @JvmField val isMoving = SimpleBooleanProperty()

    init {
        addListener(this)

        eventBus.subscribe(this)
    }

    override fun changed(
        observable: ObservableValue<out FilterWheel>,
        oldValue: FilterWheel?, newValue: FilterWheel?,
    ) {
        if (newValue == null) {
            reset()
        } else {
            isConnected.value = newValue.isConnected
            slotCount.value = newValue.slotCount
            position.value = newValue.position
            isMoving.value = newValue.isMoving
        }
    }

    fun reset() {
        isConnected.value = false
        slotCount.value = 0
        position.value = -1
        isMoving.value = false
    }

    override fun accept(event: Any) {
        if (event is DeviceEvent<*> && event.device === value) {
            Platform.runLater {
                when (event) {
                    is DeviceConnected,
                    is DeviceDisconnected -> isConnected.value = value.isConnected
                    is FilterWheelSlotCountChanged -> slotCount.value = value.slotCount
                    is FilterWheelPositionChanged -> position.value = value.position
                    is FilterWheelFilterIsMovingChanged -> isMoving.value = value.isMoving
                }
            }
        }
    }
}
