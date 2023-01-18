package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
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

    override fun changed(value: Focuser) {
        isMoving.value = value.isMoving
        position.value = value.position
        canAbsoluteMove.value = value.canAbsoluteMove
        canRelativeMove.value = value.canRelativeMove
        canAbort.value = value.canAbort
        canReverse.value = value.canReverse
        isReverse.value = value.isReverse
        canSync.value = value.canSync
        hasBackslash.value = value.hasBackslash
        maxPosition.value = value.maxPosition
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
            is FocuserPositionChanged -> Platform.runLater { position.set(device.position) }
            is FocuserCanAbsoluteMoveChanged -> Platform.runLater { canAbsoluteMove.set(device.canAbsoluteMove) }
            is FocuserCanRelativeMoveChanged -> Platform.runLater { canRelativeMove.set(device.canRelativeMove) }
            is FocuserCanAbortChanged -> Platform.runLater { canAbort.set(device.canAbort) }
            is FocuserCanReverseChanged -> Platform.runLater { canReverse.set(device.canReverse) }
            is FocuserReverseChanged -> Platform.runLater { isReverse.set(device.isReverse) }
            is FocuserCanSyncChanged -> Platform.runLater { canSync.set(device.canSync) }
            is FocuserMaxPositionChanged -> Platform.runLater { maxPosition.set(device.maxPosition) }
            is FocuserMovingChanged -> Platform.runLater { isMoving.set(device.isMoving) }
        }
    }
}
