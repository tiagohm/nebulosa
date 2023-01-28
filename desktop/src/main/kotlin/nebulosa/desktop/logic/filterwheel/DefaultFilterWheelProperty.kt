package nebulosa.desktop.logic.filterwheel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelCountChanged
import nebulosa.indi.device.filterwheel.FilterWheelMovingChanged
import nebulosa.indi.device.filterwheel.FilterWheelPositionChanged

open class DefaultFilterWheelProperty : AbstractDeviceProperty<FilterWheel>(), FilterWheelProperty {

    override val countProperty = SimpleIntegerProperty(0)
    override val positionProperty = SimpleIntegerProperty(-1)
    override val movingProperty = SimpleBooleanProperty()

    override fun onChanged(prev: FilterWheel?, device: FilterWheel) {
        countProperty.set(device.count)
        positionProperty.set(device.position)
        movingProperty.set(device.moving)
    }

    override fun onReset() {
        countProperty.set(0)
        positionProperty.set(-1)
        movingProperty.set(false)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: FilterWheel) {
        when (event) {
            is FilterWheelCountChanged -> countProperty.set(device.count)
            is FilterWheelPositionChanged -> positionProperty.set(device.position)
            is FilterWheelMovingChanged -> movingProperty.set(device.moving)
        }
    }
}
