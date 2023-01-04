package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.devices.filterwheels.FilterWheelIsMoving
import nebulosa.indi.devices.filterwheels.FilterWheelPositionChanged
import nebulosa.indi.devices.filterwheels.FilterWheelSlotCountChanged

class FilterWheelProperty : DeviceProperty<FilterWheel>() {

    @JvmField val slotCount = SimpleIntegerProperty(0)
    @JvmField val position = SimpleIntegerProperty(-1)
    @JvmField val isMoving = SimpleBooleanProperty()

    override fun changed(value: FilterWheel) {
        slotCount.set(value.slotCount)
        position.set(value.position)
        isMoving.set(value.isMoving)
    }

    override fun reset() {
        slotCount.set(0)
        position.set(-1)
        isMoving.set(false)
    }

    override fun accept(event: DeviceEvent<*>) {
        when (event) {
            is FilterWheelSlotCountChanged -> Platform.runLater { slotCount.set(value.slotCount) }
            is FilterWheelPositionChanged -> Platform.runLater { position.set(value.position) }
            is FilterWheelIsMoving -> Platform.runLater { isMoving.set(value.isMoving) }
        }
    }
}
