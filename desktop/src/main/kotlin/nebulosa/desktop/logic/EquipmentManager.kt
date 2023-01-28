package nebulosa.desktop.logic

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.logic.camera.DefaultCameraProperty
import nebulosa.desktop.logic.connection.Connected
import nebulosa.desktop.logic.connection.ConnectionEvent
import nebulosa.desktop.logic.connection.Disconnected
import nebulosa.desktop.logic.filterwheel.DefaultFilterWheelProperty
import nebulosa.desktop.logic.focuser.DefaultFocuserProperty
import nebulosa.desktop.logic.gps.DefaultGPSProperty
import nebulosa.desktop.logic.mount.DefaultMountProperty
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSAttached
import nebulosa.indi.device.gps.GPSDetached
import nebulosa.indi.device.guider.Guider
import nebulosa.indi.device.guider.GuiderAttached
import nebulosa.indi.device.guider.GuiderDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import org.koin.core.component.KoinComponent
import java.io.Closeable

class EquipmentManager : KoinComponent, Closeable {

    private val subscribers = arrayOfNulls<Disposable>(2)

    @JvmField val connectedProperty = SimpleBooleanProperty(false)

    @JvmField val attachedCameras = SimpleListProperty(FXCollections.observableArrayList<Camera>())
    @JvmField val attachedMounts = SimpleListProperty(FXCollections.observableArrayList<Mount>())
    @JvmField val attachedFilterWheels = SimpleListProperty(FXCollections.observableArrayList<FilterWheel>())
    @JvmField val attachedFocusers = SimpleListProperty(FXCollections.observableArrayList<Focuser>())
    @JvmField val attachedGPSs = SimpleListProperty(FXCollections.observableArrayList<GPS>())

    @JvmField val attachedGuiders = SimpleListProperty(FXCollections.observableArrayList<Guider>())
    @JvmField val attachedThermometers = SimpleListProperty(FXCollections.observableArrayList<Thermometer>())

    @JvmField val selectedCamera = DefaultCameraProperty()
    @JvmField val selectedMount = DefaultMountProperty()
    @JvmField val selectedFilterWheel = DefaultFilterWheelProperty()
    @JvmField val selectedFocuser = DefaultFocuserProperty()
    @JvmField val selectedGPS = DefaultGPSProperty()

    init {
        subscribers[0] = EventBus.DEVICE
            .subscribe(observeOnJavaFX = true, next = ::onDeviceEvent)
        subscribers[1] = EventBus.CONNECTION
            .subscribe(observeOnJavaFX = true, next = ::onConnectionEvent)
    }

    private fun onDeviceEvent(event: DeviceEvent<*>) {
        when (event) {
            is CameraAttached -> attachedCameras.add(event.device)
            is CameraDetached -> attachedCameras.remove(event.device)
            is MountAttached -> attachedMounts.add(event.device)
            is MountDetached -> attachedMounts.remove(event.device)
            is FilterWheelAttached -> attachedFilterWheels.add(event.device)
            is FilterWheelDetached -> attachedFilterWheels.remove(event.device)
            is FocuserAttached -> attachedFocusers.add(event.device)
            is FocuserDetached -> attachedFocusers.remove(event.device)
            is GuiderAttached -> attachedGuiders.add(event.device)
            is GuiderDetached -> attachedGuiders.remove(event.device)
            is GPSAttached -> attachedGPSs.add(event.device)
            is GPSDetached -> attachedGPSs.remove(event.device)
            is ThermometerAttached -> attachedThermometers.add(event.device)
            is ThermometerDetached -> attachedThermometers.remove(event.device)
        }
    }

    private fun onConnectionEvent(event: ConnectionEvent) {
        when (event) {
            is Connected -> connectedProperty.set(true)
            is Disconnected -> connectedProperty.set(false)
        }
    }

    override fun close() {
        subscribers.forEach { it?.dispose() }
        subscribers.fill(null)

        selectedCamera.close()
        selectedMount.close()
        selectedFilterWheel.close()
        selectedFocuser.close()
        selectedGPS.close()
    }
}
