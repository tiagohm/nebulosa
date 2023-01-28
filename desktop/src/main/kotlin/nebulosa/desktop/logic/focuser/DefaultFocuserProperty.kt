package nebulosa.desktop.logic.focuser

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.AbstractDeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.*
import nebulosa.indi.device.thermometer.ThermometerTemperatureChanged

open class DefaultFocuserProperty : AbstractDeviceProperty<Focuser>(), FocuserProperty {

    override val movingProperty = SimpleBooleanProperty()
    override val positionProperty = SimpleIntegerProperty()
    override val canAbsoluteMoveProperty = SimpleBooleanProperty()
    override val canRelativeMoveProperty = SimpleBooleanProperty()
    override val canAbortProperty = SimpleBooleanProperty()
    override val canReverseProperty = SimpleBooleanProperty()
    override val reverseProperty = SimpleBooleanProperty()
    override val canSyncProperty = SimpleBooleanProperty()
    override val hasBackslashProperty = SimpleBooleanProperty()
    override val maxPositionProperty = SimpleIntegerProperty()
    override val temperatureProperty = SimpleDoubleProperty()

    override fun onChanged(prev: Focuser?, device: Focuser) {
        movingProperty.set(device.moving)
        positionProperty.set(device.position)
        canAbsoluteMoveProperty.set(device.canAbsoluteMove)
        canRelativeMoveProperty.set(device.canRelativeMove)
        canAbortProperty.set(device.canAbort)
        canReverseProperty.set(device.canReverse)
        reverseProperty.set(device.reverse)
        canSyncProperty.set(device.canSync)
        hasBackslashProperty.set(device.hasBackslash)
        maxPositionProperty.set(device.maxPosition)
        temperatureProperty.set(device.temperature)
    }

    override fun onReset() {
        movingProperty.set(false)
        positionProperty.set(0)
        canAbsoluteMoveProperty.set(false)
        canRelativeMoveProperty.set(false)
        canAbortProperty.set(false)
        canReverseProperty.set(false)
        reverseProperty.set(false)
        canSyncProperty.set(false)
        hasBackslashProperty.set(false)
        maxPositionProperty.set(0)
        temperatureProperty.set(0.0)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Focuser) {
        when (event) {
            is FocuserPositionChanged -> positionProperty.set(device.position)
            is FocuserCanAbsoluteMoveChanged -> canAbsoluteMoveProperty.set(device.canAbsoluteMove)
            is FocuserCanRelativeMoveChanged -> canRelativeMoveProperty.set(device.canRelativeMove)
            is FocuserCanAbortChanged -> canAbortProperty.set(device.canAbort)
            is FocuserCanReverseChanged -> canReverseProperty.set(device.canReverse)
            is FocuserReverseChanged -> reverseProperty.set(device.reverse)
            is FocuserCanSyncChanged -> canSyncProperty.set(device.canSync)
            is FocuserMaxPositionChanged -> maxPositionProperty.set(device.maxPosition)
            is FocuserMovingChanged -> movingProperty.set(device.moving)
            is ThermometerTemperatureChanged -> temperatureProperty.set(device.temperature)
        }
    }
}
