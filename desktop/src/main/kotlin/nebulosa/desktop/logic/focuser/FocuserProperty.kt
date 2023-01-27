package nebulosa.desktop.logic.focuser

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.focusers.Focuser

interface FocuserProperty : DeviceProperty<Focuser> {

    val movingProperty: SimpleBooleanProperty
    val positionProperty: SimpleIntegerProperty
    val canAbsoluteMoveProperty: SimpleBooleanProperty
    val canRelativeMoveProperty: SimpleBooleanProperty
    val canAbortProperty: SimpleBooleanProperty
    val canReverseProperty: SimpleBooleanProperty
    val reverseProperty: SimpleBooleanProperty
    val canSyncProperty: SimpleBooleanProperty
    val hasBackslashProperty: SimpleBooleanProperty
    val maxPositionProperty: SimpleIntegerProperty
    val temperatureProperty: SimpleDoubleProperty

    val moving
        get() = movingProperty.get()

    val position
        get() = positionProperty.get()

    val canAbsoluteMove
        get() = canAbsoluteMoveProperty.get()

    val canRelativeMove
        get() = canRelativeMoveProperty.get()

    val canAbort
        get() = canAbortProperty.get()

    val canReverse
        get() = canReverseProperty.get()

    val reverse
        get() = reverseProperty.get()

    val canSync
        get() = canSyncProperty.get()

    val hasBackslash
        get() = hasBackslashProperty.get()

    val maxPosition
        get() = maxPositionProperty.get()

    val temperature
        get() = temperatureProperty.get()
}
