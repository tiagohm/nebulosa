package nebulosa.desktop.equipments

import io.reactivex.rxjava3.functions.Consumer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import nebulosa.desktop.cameras.AutoSubFolderMode
import nebulosa.desktop.cameras.CameraExposureTask
import nebulosa.desktop.connections.Connected
import nebulosa.desktop.connections.Disconnected
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.devices.cameras.Camera
import nebulosa.indi.devices.cameras.CameraAttached
import nebulosa.indi.devices.cameras.CameraDetached
import nebulosa.indi.devices.cameras.FrameType
import nebulosa.indi.devices.filterwheels.FilterWheel
import nebulosa.indi.devices.filterwheels.FilterWheelAttached
import nebulosa.indi.devices.filterwheels.FilterWheelDetached
import nebulosa.indi.devices.focusers.Focuser
import nebulosa.indi.devices.focusers.FocuserAttached
import nebulosa.indi.devices.focusers.FocuserDetached
import nebulosa.indi.devices.guiders.Guider
import nebulosa.indi.devices.guiders.GuiderAttached
import nebulosa.indi.devices.guiders.GuiderDetached
import nebulosa.indi.devices.mounts.Mount
import nebulosa.indi.devices.mounts.MountAttached
import nebulosa.indi.devices.mounts.MountDetached
import nebulosa.indi.devices.thermometers.Thermometer
import nebulosa.indi.devices.thermometers.ThermometerAttached
import nebulosa.indi.devices.thermometers.ThermometerDetached
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.Closeable
import java.nio.file.Path

class EquipmentManager : KoinComponent, Consumer<Any>, Closeable {

    private val eventBus by inject<EventBus>()

    @JvmField val connected = SimpleBooleanProperty(false)

    @JvmField val attachedCameras = SimpleListProperty(FXCollections.observableArrayList<Camera>())
    @JvmField val attachedMounts = SimpleListProperty(FXCollections.observableArrayList<Mount>())
    @JvmField val attachedFilterWheels = SimpleListProperty(FXCollections.observableArrayList<FilterWheel>())
    @JvmField val attachedFocusers = SimpleListProperty(FXCollections.observableArrayList<Focuser>())

    @JvmField val attachedGuiders = SimpleListProperty(FXCollections.observableArrayList<Guider>())
    @JvmField val attachedThermometers = SimpleListProperty(FXCollections.observableArrayList<Thermometer>())

    @JvmField val selectedCamera = CameraProperty()
    @JvmField val selectedMount = MountProperty()
    @JvmField val selectedFilterWheel = FilterWheelProperty()
    @JvmField val selectedFocuser = FocuserProperty()

    @Volatile var imagingCameraTask: CameraExposureTask? = null
        private set

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
            is GuiderAttached -> attachedGuiders.add(event.device)
            is GuiderDetached -> attachedGuiders.remove(event.device)
            is ThermometerAttached -> attachedThermometers.add(event.device)
            is ThermometerDetached -> attachedThermometers.remove(event.device)
            is Connected -> connected.set(true)
            is Disconnected -> connected.set(false)
        }
    }

    override fun close() {
        selectedCamera.close()
        selectedMount.close()
        selectedFilterWheel.close()
        selectedFocuser.close()
    }

    @Synchronized
    fun startImagingCapture(
        camera: Camera,
        filterWheel: FilterWheel?,
        exposure: Long, amount: Int, delay: Long,
        x: Int, y: Int, width: Int, height: Int,
        frameFormat: String, frameType: FrameType,
        binX: Int, binY: Int,
        gain: Int, offset: Int,
        save: Boolean = false, savePath: Path? = null,
        autoSubFolderMode: AutoSubFolderMode = AutoSubFolderMode.NOON,
    ): CameraExposureTask? {
        return if (imagingCameraTask == null || imagingCameraTask!!.isDone) {
            val task = CameraExposureTask(
                camera, filterWheel,
                exposure, amount, delay,
                x, y, width, height,
                frameFormat, frameType,
                binX, binY,
                gain, offset,
                save, savePath, autoSubFolderMode,
            )

            imagingCameraTask = task

            task.execute()
                .whenComplete { _, _ -> imagingCameraTask = null }

            task
        } else {
            null
        }
    }
}
