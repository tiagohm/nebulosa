package nebulosa.desktop.cameras

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.DirectoryChooser
import nebulosa.desktop.home.Home
import nebulosa.desktop.internal.Icon
import nebulosa.desktop.internal.Screen
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceDisconnected
import nebulosa.indi.devices.cameras.Camera
import org.koin.core.component.inject

class CameraManager : Screen("CameraManager") {

    private val home by inject<Home>()

    @FXML private lateinit var cameras: ChoiceBox<Camera>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var cameraMenuButton: Label
    @FXML private lateinit var cameraMenu: ContextMenu

    private val connected = SimpleBooleanProperty(false)
    private val connecting = SimpleBooleanProperty(false)

    @Volatile private var subscriber: Disposable? = null

    init {
        isResizable = false

        connect.disableProperty().bind(cameras.selectionModel.selectedItemProperty().isNull.or(connecting))
        cameraMenuButton.disableProperty().bind(connect.disableProperty())

        connected.addListener { _, _, value -> connect.graphic = if (value) Icon.closeCircle() else Icon.connection() }

        cameras.selectionModel.selectedItemProperty().addListener { _, _, value ->
            connected.set(value?.isConnected ?: false)
            eventBus.post(CameraSelected(value))
        }

        cameraMenuButton.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                cameraMenu.show(cameraMenuButton, it.screenX, it.screenY)
                it.consume()
            }
        }
    }

    private val selectedCamera: Camera? get() = cameras.selectionModel.selectedItem

    override fun onStart() {
        subscriber = eventBus.subscribe(this)

        val camera = selectedCamera

        cameras.items.clear()
        cameras.items.addAll(home.attachedCameras)

        if (camera !in home.attachedCameras) {
            cameras.selectionModel.select(null)
        } else {
            cameras.selectionModel.select(camera)
        }

        connected.set(selectedCamera?.isConnected == true)
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    override fun onEvent(event: Any) {
        when (event) {
            is DeviceConnected -> {
                if (event.device === selectedCamera) {
                    Platform.runLater {
                        connecting.set(false)
                        connected.set(true)
                    }
                }
            }
            is DeviceDisconnected -> {
                if (event.device === selectedCamera) {
                    Platform.runLater {
                        connecting.set(false)
                        connected.set(false)
                    }
                }
            }
        }
    }

    @FXML
    private fun connect() {
        if (!connected.get()) {
            connecting.set(true)
            selectedCamera!!.connect()
        } else {
            selectedCamera!!.disconnect()
        }
    }

    @FXML
    private fun toggleAutoSaveAllExposures() {

    }

    @FXML
    private fun toggleAutoSubFolder() {

    }

    @FXML
    private fun openImageSavePath() {
        val chooser = DirectoryChooser()
        chooser.title = "Open Image Save Path"
        val file = chooser.showDialog(null) ?: return
        println(file)
    }

    @FXML
    private fun chooseNewSubFolderAt(event: ActionEvent) {
        val menuItem = event.source as CheckMenuItem
        val isNoon = menuItem.userData == "NOON"
        menuItem.parentMenu.items.onEach { (it as CheckMenuItem).isSelected = it === menuItem }
        println(isNoon)
    }

    @FXML
    private fun applyTemperatureSetpoint() {
    }
}
