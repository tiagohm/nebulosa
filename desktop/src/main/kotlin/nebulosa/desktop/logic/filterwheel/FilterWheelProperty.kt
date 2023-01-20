package nebulosa.desktop.logic.filterwheel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheels.FilterWheel
import nebulosa.indi.device.filterwheels.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheels.FilterWheelPositionChanged
import nebulosa.indi.device.filterwheels.FilterWheelSlotCountChanged

open class FilterWheelProperty : DeviceProperty<FilterWheel>() {

    @JvmField val slotCount = SimpleIntegerProperty(0)
    @JvmField val position = SimpleIntegerProperty(-1)
    @JvmField val isMoving = SimpleBooleanProperty()

    override fun onChanged(prev: FilterWheel?, new: FilterWheel) {
        slotCount.set(new.slotCount)
        position.set(new.position)
        isMoving.set(new.isMoving)
    }

    override fun reset() {
        slotCount.set(0)
        position.set(-1)
        isMoving.set(false)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>) {
        super.onDeviceEvent(event)

        when (event) {
            is FilterWheelSlotCountChanged -> slotCount.set(value.slotCount)
            is FilterWheelPositionChanged -> position.set(value.position)
            is FilterWheelMovingChanged -> isMoving.set(value.isMoving)
        }
    }
}
