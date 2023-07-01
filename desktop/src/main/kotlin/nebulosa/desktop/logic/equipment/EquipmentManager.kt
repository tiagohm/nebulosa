package nebulosa.desktop.logic.equipment

import jakarta.annotation.PostConstruct
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.helper.runBlockingMain
import nebulosa.desktop.logic.camera.DefaultCameraProperty
import nebulosa.desktop.logic.connection.Connected
import nebulosa.desktop.logic.connection.ConnectionEvent
import nebulosa.desktop.logic.connection.Disconnected
import nebulosa.desktop.logic.filterwheel.DefaultFilterWheelProperty
import nebulosa.desktop.logic.focuser.DefaultFocuserProperty
import nebulosa.desktop.logic.gps.DefaultGPSProperty
import nebulosa.desktop.logic.guider.DefaultGuideOutputProperty
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
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.Closeable

@Service
class EquipmentManager : Closeable {

    @Autowired private lateinit var eventBus: EventBus

    val connectedProperty = SimpleBooleanProperty(false)

    val attachedCameras = SimpleListProperty(FXCollections.observableArrayList<Camera>())
    val attachedMounts = SimpleListProperty(FXCollections.observableArrayList<Mount>())
    val attachedFilterWheels = SimpleListProperty(FXCollections.observableArrayList<FilterWheel>())
    val attachedFocusers = SimpleListProperty(FXCollections.observableArrayList<Focuser>())
    val attachedGPSs = SimpleListProperty(FXCollections.observableArrayList<GPS>())

    val attachedGuideOutputs = SimpleListProperty(FXCollections.observableArrayList<GuideOutput>())
    val attachedThermometers = SimpleListProperty(FXCollections.observableArrayList<Thermometer>())

    val selectedCamera = DefaultCameraProperty()
    val selectedGuideCamera = DefaultCameraProperty()
    val selectedMount = DefaultMountProperty()
    val selectedGuideMount = DefaultMountProperty()
    val selectedFilterWheel = DefaultFilterWheelProperty()
    val selectedFocuser = DefaultFocuserProperty()
    val selectedGPS = DefaultGPSProperty()
    val selectedGuideOutput = DefaultGuideOutputProperty()

    @PostConstruct
    private fun initialize() {
        selectedCamera.initialize()
        selectedGuideCamera.initialize()
        selectedMount.initialize()
        selectedGuideMount.initialize()
        selectedFilterWheel.initialize()
        selectedFocuser.initialize()
        selectedGPS.initialize()
        selectedGuideOutput.initialize()

        eventBus.register(selectedCamera)
        eventBus.register(selectedGuideCamera)
        eventBus.register(selectedMount)
        eventBus.register(selectedGuideMount)
        eventBus.register(selectedFilterWheel)
        eventBus.register(selectedFocuser)
        eventBus.register(selectedGPS)
        eventBus.register(selectedGuideOutput)
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    final fun onDeviceEvent(event: DeviceEvent<*>): Unit = runBlockingMain {
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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    final fun onConnectionEvent(event: ConnectionEvent) = runBlockingMain {
        when (event) {
            is Connected -> connectedProperty.set(true)
            is Disconnected -> connectedProperty.set(false)
        }
    }

    override fun close() {
        selectedCamera.close()
        selectedGuideCamera.close()
        selectedMount.close()
        selectedGuideMount.close()
        selectedFilterWheel.close()
        selectedFocuser.close()
        selectedGPS.close()
        selectedGuideOutput.close()
    }
}
