package nebulosa.desktop.cameras

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.DirectoryChooser
import nebulosa.desktop.core.controls.Icon
import nebulosa.desktop.core.controls.Screen
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.indi.devices.PropertyChangedEvent
import nebulosa.indi.devices.cameras.*
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class CameraManagerScreen : Screen("CameraManager", "nebulosa-camera-manager") {

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
    @FXML private lateinit var exposure: Spinner<Double>
    @FXML private lateinit var exposureUnit: ToggleGroup
    @FXML private lateinit var exposureType: ToggleGroup
    @FXML private lateinit var exposureDelay: Spinner<Double>
    @FXML private lateinit var exposureCount: Spinner<Double>
    @FXML private lateinit var subframe: ToggleSwitch
    @FXML private lateinit var fullsize: Button
    @FXML private lateinit var x: Spinner<Double>
    @FXML private lateinit var y: Spinner<Double>
    @FXML private lateinit var width: Spinner<Double>
    @FXML private lateinit var height: Spinner<Double>
    @FXML private lateinit var binX: Spinner<Double>
    @FXML private lateinit var binY: Spinner<Double>
    @FXML private lateinit var frameType: ChoiceBox<FrameType>
    @FXML private lateinit var frameFormat: ChoiceBox<String>
    @FXML private lateinit var startCapture: Button
    @FXML private lateinit var abortCapture: Button

    private val connecting = SimpleBooleanProperty(false)

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Camera"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedCamera.isConnected.not()
        cameras.disableProperty().bind(connecting)
        equipmentManager.selectedCamera.bind(cameras.selectionModel.selectedItemProperty())
        connect.disableProperty().bind(equipmentManager.selectedCamera.isNull.or(connecting))
        cameraMenuIcon.disableProperty().bind(isNotConnected)
        cooler.disableProperty().bind(isNotConnected.or(equipmentManager.selectedCamera.hasCooler.not()))
        dewHeater.disableProperty().bind(isNotConnected.or(equipmentManager.selectedCamera.hasDewHeater.not()))
        temperatureSetpoint.disableProperty().bind(isNotConnected.or(equipmentManager.selectedCamera.canSetTemperature.not()))
        applyTemperatureSetpoint.disableProperty().bind(temperatureSetpoint.disableProperty())
        exposure.disableProperty().bind(isNotConnected)
        exposureUnit.toggles.forEach { (it as RadioButton).disableProperty().bind(exposure.disableProperty()) }
        exposureType.toggles.forEach { (it as RadioButton).disableProperty().bind(exposure.disableProperty()) }
        val fixed = exposureType.toggles.first { it.userData == "FIXED" } as RadioButton
        val continuous = exposureType.toggles.first { it.userData == "CONTINUOUS" } as RadioButton
        exposureDelay.disableProperty().bind(
            fixed.disableProperty().and(continuous.disableProperty()).or(fixed.selectedProperty().not().and(continuous.selectedProperty().not()))
        )
        exposureCount.disableProperty().bind(fixed.disableProperty().or(fixed.selectedProperty().not()))
        subframe.disableProperty().bind(isNotConnected.or(equipmentManager.selectedCamera.canSubFrame.not()))
        fullsize.disableProperty().bind(subframe.disableProperty().or(subframe.selectedProperty().not()))
        x.disableProperty().bind(fullsize.disableProperty())
        y.disableProperty().bind(x.disableProperty())
        width.disableProperty().bind(x.disableProperty())
        height.disableProperty().bind(x.disableProperty())
        binX.disableProperty().bind(isNotConnected.or(equipmentManager.selectedCamera.canBin.not()))
        binY.disableProperty().bind(binX.disableProperty())
        frameType.disableProperty().bind(isNotConnected)
        frameFormat.disableProperty().bind(isNotConnected)
        startCapture.disableProperty().bind(isNotConnected)
        abortCapture.disableProperty().bind(isNotConnected.or(startCapture.disableProperty().not()))

        cooler.selectedProperty().bind(equipmentManager.selectedCamera.isCoolerOn)
        dewHeater.selectedProperty().bind(equipmentManager.selectedCamera.isDewHeaterOn)
        temperature.textProperty().bind(equipmentManager.selectedCamera.temperature.asString(Locale.ENGLISH, "Temperature (%.1f °C)"))
        frameFormat.itemsProperty().bind(equipmentManager.selectedCamera.frameFormats)

        equipmentManager.selectedCamera.addListener { _, _, value ->
            title = "Camera - ${value.name}"

            updateExposure()
            updateFrame()
            updateFrameFormat()
            updateBin()
        }

        equipmentManager.selectedCamera.isConnected.addListener { _, _, value ->
            connecting.set(false)

            connect.graphic = if (value) Icon.closeCircle() else Icon.connection()
        }

        cameraMenuIcon.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                cameraMenu.show(cameraMenuIcon, it.screenX, it.screenY)
                it.consume()
            }
        }

        exposure.userData = TimeUnit.MICROSECONDS
    }

    override fun onStart() {
        subscriber = eventBus
            .filter { it is CameraEvent && it is PropertyChangedEvent }
            .subscribe(this)

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
            is CameraExposureMinMaxChanged -> Platform.runLater(::updateExposure)
            is CameraFrameChanged -> Platform.runLater(::updateFrame)
            is CameraCanBinChanged -> Platform.runLater(::updateBin)
            is CameraFrameFormatsChanged -> Platform.runLater(::updateFrameFormat)
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

    @FXML
    private fun updateExposureUnit(event: ActionEvent) {
        val radio = event.source as RadioButton
        val timeUnit = TimeUnit.valueOf(radio.userData as String)
        val prevTimeUnit = exposure.userData as TimeUnit
        updateExposureUnit(prevTimeUnit, timeUnit, exposure.value)
    }

    @Synchronized
    private fun updateExposureUnit(from: TimeUnit, to: TimeUnit, exposureValue: Double) {
        val minValue = max(1L, to.convert(equipmentManager.selectedCamera.exposureMin.value, TimeUnit.MICROSECONDS))
        val maxValue = to.convert(equipmentManager.selectedCamera.exposureMax.value, TimeUnit.MICROSECONDS)
        with(exposure.valueFactory as DoubleSpinnerValueFactory) {
            max = maxValue.toDouble()
            min = minValue.toDouble()
            value = to.convert(exposureValue.toLong(), from).toDouble()
            exposure.userData = to
        }
    }

    private fun updateExposure() {
        val timeUnit = exposure.userData as TimeUnit
        updateExposureUnit(timeUnit, timeUnit, exposure.value)
    }

    @Synchronized
    private fun updateFrame() {
        val camera = equipmentManager.selectedCamera.value ?: return

        with(x.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxX.toDouble()
            min = camera.minX.toDouble()
            if (!subframe.isSelected) value = min
        }
        with(y.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxY.toDouble()
            min = camera.minY.toDouble()
            if (!subframe.isSelected) value = min
        }
        with(width.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxWidth.toDouble()
            min = camera.minWidth.toDouble()
            if (!subframe.isSelected) value = max
        }
        with(height.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxHeight.toDouble()
            min = camera.minHeight.toDouble()
            if (!subframe.isSelected) value = max
        }
    }

    @Synchronized
    private fun updateFrameFormat() {
        val selectedFrameFormat = frameFormat.selectionModel.selectedItem

        if (selectedFrameFormat == null || selectedFrameFormat !in equipmentManager.selectedCamera.frameFormats) {
            frameFormat.selectionModel.selectFirst()
        }
    }

    private fun updateBin() {
        val camera = equipmentManager.selectedCamera.value ?: return

        (binX.valueFactory as DoubleSpinnerValueFactory).max = camera.maxBinX.toDouble()
        (binY.valueFactory as DoubleSpinnerValueFactory).max = camera.maxBinY.toDouble()
    }

    @FXML
    private fun applyFullsize() {
        val camera = equipmentManager.selectedCamera.value ?: return

        (x.valueFactory as DoubleSpinnerValueFactory).value = camera.minX.toDouble()
        (y.valueFactory as DoubleSpinnerValueFactory).value = camera.minY.toDouble()
        (width.valueFactory as DoubleSpinnerValueFactory).value = camera.maxWidth.toDouble()
        (height.valueFactory as DoubleSpinnerValueFactory).value = camera.maxHeight.toDouble()
    }

    @FXML
    private fun startCapture() {
    }

    @FXML
    private fun abortCapture() {
    }
}
