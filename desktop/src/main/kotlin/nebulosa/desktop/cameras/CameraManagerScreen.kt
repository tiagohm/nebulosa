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
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.DeviceConnected
import nebulosa.indi.devices.DeviceDisconnected
import nebulosa.indi.devices.cameras.Camera
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject
import java.util.*

class CameraManagerScreen : Screen("CameraManager") {

    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var cameras: ChoiceBox<Camera>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var cameraMenuIcon: Label
    @FXML private lateinit var cameraMenu: ContextMenu
    @FXML private lateinit var cooler: ToggleSwitch
    @FXML private lateinit var dewHeater: ToggleSwitch
    @FXML private lateinit var temperature: Label
    @FXML private lateinit var temperatureSetpoint: Spinner<Double>
    @FXML private lateinit var applyTemperatureSetpoint: Button

    private val connecting = SimpleBooleanProperty(false)

    @Volatile private var subscriber: Disposable? = null

    init {
        isResizable = false

        equipmentManager.selectedCamera.bind(cameras.selectionModel.selectedItemProperty())
        connect.disableProperty().bind(equipmentManager.selectedCamera.isNull.or(connecting))
        cameraMenuIcon.disableProperty().bind(connect.disableProperty())
        cooler.disableProperty().bind(connect.disableProperty().or(equipmentManager.selectedCamera.hasCooler.not()))
        dewHeater.disableProperty().bind(connect.disableProperty().or(equipmentManager.selectedCamera.hasCooler.not()))
        temperatureSetpoint.disableProperty().bind(connect.disableProperty().or(equipmentManager.selectedCamera.canSetTemperature.not()))
        applyTemperatureSetpoint.disableProperty().bind(temperatureSetpoint.disableProperty())

        cooler.selectedProperty().bind(equipmentManager.selectedCamera.isCoolerOn)
        dewHeater.selectedProperty().bind(equipmentManager.selectedCamera.isCoolerOn)
        temperature.textProperty().bind(equipmentManager.selectedCamera.temperature.asString(Locale.ENGLISH, "Temperature %.1f °C"))

        equipmentManager.selectedCamera.isConnected.addListener { _, _, value ->
            connect.graphic = if (value) Icon.closeCircle() else Icon.connection()
        }

        cameraMenuIcon.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                cameraMenu.show(cameraMenuIcon, it.screenX, it.screenY)
                it.consume()
            }
        }
    }

    override fun onStart() {
        subscriber = eventBus.subscribe(this)

        val camera = equipmentManager.selectedCamera.value

        cameras.items.clear()
        cameras.items.addAll(equipmentManager.attachedCameras)

        if (camera !in equipmentManager.attachedCameras) {
            cameras.selectionModel.select(null)
        } else {
            cameras.selectionModel.select(camera)
        }
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null
    }

    override fun onEvent(event: Any) {
        when (event) {
            is DeviceConnected -> {
                if (event.device === equipmentManager.selectedCamera.value) {
                    Platform.runLater {
                        connecting.set(false)
                    }
                }
            }
            is DeviceDisconnected -> {
                if (event.device === equipmentManager.selectedCamera.value) {
                    Platform.runLater {
                        connecting.set(false)
                    }
                }
            }
        }
    }

    @FXML
    private fun connect() {
        if (!equipmentManager.selectedCamera.isConnected.value) {
            connecting.set(true)
            equipmentManager.selectedCamera.value!!.connect()
        } else {
            equipmentManager.selectedCamera.value!!.disconnect()
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
        println(temperatureSetpoint.value)
    }
}
