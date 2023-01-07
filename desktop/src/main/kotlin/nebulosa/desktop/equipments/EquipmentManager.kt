package nebulosa.desktop.equipments

import io.reactivex.rxjava3.functions.Consumer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.connections.Connected
import nebulosa.desktop.connections.Disconnected
import nebulosa.desktop.core.EventBus
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraAttached
import nebulosa.indi.devices.cameras.CameraDetached
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.devices.filterwheels.FilterWheelAttached
import nebulosa.indi.devices.filterwheels.FilterWheelDetached
import nebulosa.indi.devices.focusers.Focuser
import nebulosa.indi.devices.focusers.FocuserAttached
import nebulosa.indi.devices.focusers.FocuserDetached
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountAttached
import nebulosa.indi.devices.mounts.MountDetached
import nebulosa.indi.devices.thermometers.Thermometer
import nebulosa.indi.devices.thermometers.ThermometerAttached
import nebulosa.indi.devices.thermometers.ThermometerDetached
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EquipmentManager : KoinComponent, Consumer<Any> {

    private val eventBus by inject<EventBus>()

    @JvmField val connected = SimpleBooleanProperty(false)

    @JvmField val attachedCameras = SimpleListProperty(FXCollections.observableArrayList<Camera>())
    @JvmField val attachedMounts = SimpleListProperty(FXCollections.observableArrayList<Mount>())
    @JvmField val attachedFilterWheels = SimpleListProperty(FXCollections.observableArrayList<FilterWheel>())
    @JvmField val attachedFocusers = SimpleListProperty(FXCollections.observableArrayList<Focuser>())

    @JvmField val attachedThermometers = SimpleListProperty(FXCollections.observableArrayList<Thermometer>())

    @JvmField val selectedCamera = CameraProperty()
    @JvmField val selectedMount = MountProperty()
    @JvmField val selectedFilterWheel = FilterWheelProperty()
    @JvmField val selectedFocuser = FocuserProperty()

    init {
        eventBus.subscribe(this)
    }

    override fun accept(event: Any) {
        when (event) {
            is CameraAttached -> attachedCameras.add(event.device)
            is CameraDetached -> attachedCameras.remove(event.device)
            is MountAttached -> attachedMounts.add(event.device)
            is MountDetached -> attachedMounts.remove(event.device)
            is FilterWheelAttached -> attachedFilterWheels.add(event.device)
            is FilterWheelDetached -> attachedFilterWheels.remove(event.device)
            is FocuserAttached -> attachedFocusers.add(event.device)
            is FocuserDetached -> attachedFocusers.remove(event.device)
            is ThermometerAttached -> attachedThermometers.add(event.device)
            is ThermometerDetached -> attachedThermometers.remove(event.device)
            is Connected -> connected.set(true)
            is Disconnected -> connected.set(false)
        }
    }
}
