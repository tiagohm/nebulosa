package nebulosa.desktop.logic.focuser

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focusers.*
import nebulosa.indi.device.thermometers.ThermometerTemperatureChanged

open class FocuserProperty : DeviceProperty<Focuser>() {

    @JvmField val isMoving = SimpleBooleanProperty()
    @JvmField val position = SimpleIntegerProperty()
    @JvmField val canAbsoluteMove = SimpleBooleanProperty()
    @JvmField val canRelativeMove = SimpleBooleanProperty()
    @JvmField val canAbort = SimpleBooleanProperty()
    @JvmField val canReverse = SimpleBooleanProperty()
    @JvmField val isReverse = SimpleBooleanProperty()
    @JvmField val canSync = SimpleBooleanProperty()
    @JvmField val hasBackslash = SimpleBooleanProperty()
    @JvmField val maxPosition = SimpleIntegerProperty()
    @JvmField val temperature = SimpleDoubleProperty()

    override fun onChanged(prev: Focuser?, new: Focuser) {
        isMoving.value = new.isMoving
        position.value = new.position
        canAbsoluteMove.value = new.canAbsoluteMove
        canRelativeMove.value = new.canRelativeMove
        canAbort.value = new.canAbort
        canReverse.value = new.canReverse
        isReverse.value = new.isReverse
        canSync.value = new.canSync
        hasBackslash.value = new.hasBackslash
        maxPosition.value = new.maxPosition
        temperature.value = new.temperature
    }

    override fun reset() {
        isMoving.value = false
        position.value = 0
        canAbsoluteMove.value = false
        canRelativeMove.value = false
        canAbort.value = false
        canReverse.value = false
        isReverse.value = false
        canSync.value = false
        hasBackslash.value = false
        maxPosition.value = 0
        temperature.value = 0.0
    }

    override fun onDeviceEvent(event: DeviceEvent<*>) {
        super.onDeviceEvent(event)

        when (event) {
            is FocuserPositionChanged -> position.set(value.position)
            is FocuserCanAbsoluteMoveChanged -> canAbsoluteMove.set(value.canAbsoluteMove)
            is FocuserCanRelativeMoveChanged -> canRelativeMove.set(value.canRelativeMove)
            is FocuserCanAbortChanged -> canAbort.set(value.canAbort)
            is FocuserCanReverseChanged -> canReverse.set(value.canReverse)
            is FocuserReverseChanged -> isReverse.set(value.isReverse)
            is FocuserCanSyncChanged -> canSync.set(value.canSync)
            is FocuserMaxPositionChanged -> maxPosition.set(value.maxPosition)
            is FocuserMovingChanged -> isMoving.set(value.isMoving)
            is ThermometerTemperatureChanged -> temperature.set(value.temperature)
        }
    }
}
