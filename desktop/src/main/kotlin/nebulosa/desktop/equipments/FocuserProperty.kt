package nebulosa.desktop.equipments

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.focusers.*

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
        when (event) {
            is FocuserPositionChanged -> Platform.runLater { position.value = value.position }
            is FocuserCanAbsoluteMoveChanged -> Platform.runLater { canAbsoluteMove.value = value.canAbsoluteMove }
            is FocuserCanRelativeMoveChanged -> Platform.runLater { canRelativeMove.value = value.canRelativeMove }
            is FocuserCanAbortChanged -> Platform.runLater { canAbort.value = value.canAbort }
            is FocuserCanReverseChanged -> Platform.runLater { canReverse.value = value.canReverse }
            is FocuserReverseChanged -> Platform.runLater { isReverse.value = value.isReverse }
            is FocuserCanSyncChanged -> Platform.runLater { canSync.value = value.canSync }
            is FocuserMaxPositionChanged -> Platform.runLater { maxPosition.value = value.maxPosition }
            is FocuserMovingChanged -> Platform.runLater { isMoving.value = value.isMoving }
        }
    }
}
