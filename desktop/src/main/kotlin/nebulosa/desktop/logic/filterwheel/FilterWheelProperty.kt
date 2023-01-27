package nebulosa.desktop.logic.filterwheel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.filterwheels.FilterWheel

interface FilterWheelProperty : DeviceProperty<FilterWheel> {

    val countProperty: SimpleIntegerProperty
    val positionProperty: SimpleIntegerProperty
    val movingProperty: SimpleBooleanProperty

    val count
        get() = countProperty.get()

    val position
        get() = positionProperty.get()

    val moving
        get() = movingProperty.get()
}
