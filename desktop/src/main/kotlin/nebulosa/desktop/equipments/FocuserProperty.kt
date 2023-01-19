package nebulosa.desktop.equipments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.desktop.logic.DeviceProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focusers.*

class FocuserProperty : DeviceProperty<Focuser>() {

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

    override fun changed(prev: Focuser?, new: Focuser) {
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
    }

    override fun accept(event: DeviceEvent<Focuser>) {
        val device = event.device!!

        when (event) {
            is FocuserPositionChanged -> position.set(device.position)
            is FocuserCanAbsoluteMoveChanged -> canAbsoluteMove.set(device.canAbsoluteMove)
            is FocuserCanRelativeMoveChanged -> canRelativeMove.set(device.canRelativeMove)
            is FocuserCanAbortChanged -> canAbort.set(device.canAbort)
            is FocuserCanReverseChanged -> canReverse.set(device.canReverse)
            is FocuserReverseChanged -> isReverse.set(device.isReverse)
            is FocuserCanSyncChanged -> canSync.set(device.canSync)
            is FocuserMaxPositionChanged -> maxPosition.set(device.maxPosition)
            is FocuserMovingChanged -> isMoving.set(device.isMoving)
        }
    }
}
