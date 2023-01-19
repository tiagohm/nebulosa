package nebulosa.desktop.logic.camera

import javafx.beans.property.*
import nebulosa.desktop.gui.camera.AutoSubFolderMode
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.cameras.Camera
import nebulosa.indi.device.cameras.FrameType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class CameraPreferences : KoinComponent {

    private val preferences by inject<Preferences>()

    val x = SimpleIntegerProperty()
    val y = SimpleIntegerProperty()

    val autoSaveAllExposures = SimpleBooleanProperty()
    val autoSubFolder = SimpleBooleanProperty()
    val newSubFolderAt = SimpleObjectProperty(AutoSubFolderMode.NOON)
    val imageSavePath = SimpleStringProperty()
    val temperature = SimpleDoubleProperty()
    val exposureUnit = SimpleObjectProperty(TimeUnit.MICROSECONDS)
    val exposure = SimpleLongProperty()
    val exposureCount = SimpleIntegerProperty()
    val exposureMode = SimpleStringProperty()
    val exposureDelay = SimpleDoubleProperty()
    val isSubFrame = SimpleBooleanProperty()
    val frameX = SimpleDoubleProperty()
    val frameY = SimpleDoubleProperty()
    val frameWidth = SimpleDoubleProperty()
    val frameHeight = SimpleDoubleProperty()
    val frameType = SimpleObjectProperty(FrameType.LIGHT)
    val frameFormat = SimpleStringProperty()
    val binX = SimpleDoubleProperty()
    val binY = SimpleDoubleProperty()
    val gain = SimpleDoubleProperty()
    val offset = SimpleDoubleProperty()

    fun save(camera: Camera?) {
        if (camera != null) {
            preferences.bool("camera.${camera.name}.autoSaveAllExposures", autoSaveAllExposures.get())
            preferences.bool("camera.${camera.name}.autoSubFolder", autoSubFolder.get())
            preferences.enum("camera.${camera.name}.newSubFolderAt", newSubFolderAt.get())
            preferences.string("camera.${camera.name}.imageSavePath", imageSavePath.get())
        }
    }

    fun load(camera: Camera?) {
        if (camera != null) {
            autoSaveAllExposures.set(preferences.bool("camera.${camera.name}.autoSaveAllExposures"))
            autoSubFolder.set(preferences.bool("camera.${camera.name}.autoSubFolder"))
            newSubFolderAt.set(preferences.enum<AutoSubFolderMode>("camera.${camera.name}.newSubFolderAt") ?: AutoSubFolderMode.NOON)
            imageSavePath.set(preferences.string("camera.${camera.name}.imageSavePath"))
        }

        preferences.int("cameraManager.screen.x")?.let { x.set(it) }
        preferences.int("cameraManager.screen.y")?.let { y.set(it) }
    }
}
