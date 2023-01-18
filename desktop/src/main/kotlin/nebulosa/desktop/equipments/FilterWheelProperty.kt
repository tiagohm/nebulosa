package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheels.FilterWheel
import nebulosa.indi.device.filterwheels.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheels.FilterWheelPositionChanged
import nebulosa.indi.device.filterwheels.FilterWheelSlotCountChanged

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

    override fun accept(event: DeviceEvent<FilterWheel>) {
        val device = event.device!!

        when (event) {
            is FilterWheelSlotCountChanged -> Platform.runLater { slotCount.set(device.slotCount) }
            is FilterWheelPositionChanged -> Platform.runLater { position.set(device.position) }
            is FilterWheelMovingChanged -> Platform.runLater { isMoving.set(device.isMoving) }
        }
    }
}
