package nebulosa.desktop.logic.equipment

import io.reactivex.rxjava3.disposables.Disposable
import jakarta.annotation.PostConstruct
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.logic.ConnectionEventBus
import nebulosa.desktop.logic.DeviceEventBus
import nebulosa.desktop.logic.camera.DefaultCameraProperty
import nebulosa.desktop.logic.connection.Connected
import nebulosa.desktop.logic.connection.ConnectionEvent
import nebulosa.desktop.logic.connection.Disconnected
import nebulosa.desktop.logic.filterwheel.DefaultFilterWheelProperty
import nebulosa.desktop.logic.focuser.DefaultFocuserProperty
import nebulosa.desktop.logic.gps.DefaultGPSProperty
import nebulosa.desktop.logic.mount.DefaultMountProperty
import nebulosa.desktop.logic.observeOnJavaFX
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
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.Closeable

@Service
class EquipmentManager(@Autowired private val deviceEventBus: DeviceEventBus) : Closeable {

    @Autowired private lateinit var connectionEventBus: ConnectionEventBus

    private final val subscribers = arrayOfNulls<Disposable>(2)

    final val connectedProperty = SimpleBooleanProperty(false)

    final val attachedCameras = SimpleListProperty(FXCollections.observableArrayList<Camera>())
    final val attachedMounts = SimpleListProperty(FXCollections.observableArrayList<Mount>())
    final val attachedFilterWheels = SimpleListProperty(FXCollections.observableArrayList<FilterWheel>())
    final val attachedFocusers = SimpleListProperty(FXCollections.observableArrayList<Focuser>())
    final val attachedGPSs = SimpleListProperty(FXCollections.observableArrayList<GPS>())

    final val attachedGuideOutputs = SimpleListProperty(FXCollections.observableArrayList<GuideOutput>())
    final val attachedThermometers = SimpleListProperty(FXCollections.observableArrayList<Thermometer>())

    final val selectedCamera = DefaultCameraProperty()
    final val selectedMount = DefaultMountProperty()
    final val selectedFilterWheel = DefaultFilterWheelProperty()
    final val selectedFocuser = DefaultFocuserProperty()
    final val selectedGPS = DefaultGPSProperty()

    @PostConstruct
    private fun initialize() {
        selectedCamera.initialize(deviceEventBus)
        selectedMount.initialize(deviceEventBus)
        selectedFilterWheel.initialize(deviceEventBus)
        selectedFocuser.initialize(deviceEventBus)
        selectedGPS.initialize(deviceEventBus)

        subscribers[0] = deviceEventBus
            .observeOnJavaFX()
            .subscribe(::onDeviceEvent)

        subscribers[1] = connectionEventBus
            .observeOnJavaFX()
            .subscribe(::onConnectionEvent)
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
            is GuideOutputAttached -> attachedGuideOutputs.add(event.device)
            is GuideOutputDetached -> attachedGuideOutputs.remove(event.device)
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
