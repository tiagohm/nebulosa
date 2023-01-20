package nebulosa.desktop.gui.camera

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import nebulosa.desktop.core.beans.and
import nebulosa.desktop.core.beans.between
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.beans.or
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.core.util.toggle
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.camera.CameraManager
import nebulosa.indi.device.cameras.Camera
import nebulosa.indi.device.cameras.FrameType
import org.controlsfx.control.ToggleSwitch
import java.util.*
import java.util.concurrent.TimeUnit

class CameraWindow : AbstractWindow() {

    override val resourceName = "Camera"

    override val icon = "nebulosa-camera"

    @FXML private lateinit var cameraChoiceBox: ChoiceBox<Camera>
    @FXML private lateinit var connectButton: Button
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var autoSaveAllExposuresMenuItem: CheckMenuItem
    @FXML private lateinit var autoSubFolderMenuItem: CheckMenuItem
    @FXML private lateinit var newSubFolderAtNoonMenuItem: CheckMenuItem
    @FXML private lateinit var newSubFolderAtMidnightMenuItem: CheckMenuItem
    @FXML private lateinit var autoSaveAllExposuresIcon: Label
    @FXML private lateinit var autoSubFolderIcon: Label
    @FXML private lateinit var imageSavePathLabel: Label
    @FXML private lateinit var coolerToggleSwitch: ToggleSwitch
    @FXML private lateinit var dewHeaterToggleSwitch: ToggleSwitch
    @FXML private lateinit var temperatureLabel: Label
    @FXML private lateinit var temperatureSetpointSpinner: Spinner<Double>
    @FXML private lateinit var temperatureSetpointButton: Button
    @FXML private lateinit var exposureSpinner: Spinner<Double>
    @FXML private lateinit var exposureUnitToggleGroup: ToggleGroup
    @FXML private lateinit var exposureModeToggleGroup: ToggleGroup
    @FXML private lateinit var exposureDelaySpinner: Spinner<Double>
    @FXML private lateinit var exposureCountSpinner: Spinner<Double>
    @FXML private lateinit var subFrameToggleSwitch: ToggleSwitch
    @FXML private lateinit var fullsizeButton: Button
    @FXML private lateinit var frameXSpinner: Spinner<Double>
    @FXML private lateinit var frameYSpinner: Spinner<Double>
    @FXML private lateinit var frameWidthSpinner: Spinner<Double>
    @FXML private lateinit var frameHeightSpinner: Spinner<Double>
    @FXML private lateinit var binXSpinner: Spinner<Double>
    @FXML private lateinit var binYSpinner: Spinner<Double>
    @FXML private lateinit var gainSpinner: Spinner<Double>
    @FXML private lateinit var offsetSpinner: Spinner<Double>
    @FXML private lateinit var frameTypeChoiceBox: ChoiceBox<FrameType>
    @FXML private lateinit var frameFormatChoiceBox: ChoiceBox<String>
    @FXML private lateinit var startCaptureButton: Button
    @FXML private lateinit var abortCaptureButton: Button
    @FXML private lateinit var statusLabel: Label

    private val cameraManager = CameraManager(this)

    init {
        title = "Camera"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = !cameraManager.isConnected
        val isConnecting = cameraManager.isConnecting
        val isCapturing = cameraManager.isCapturing
        val isNotConnectedOrCapturing = isNotConnected or isCapturing

        cameraChoiceBox.converter = DeviceStringConverter()
        cameraChoiceBox.disableProperty().bind(isConnecting or isCapturing)
        cameraChoiceBox.itemsProperty().bind(cameraManager.cameras)
        cameraManager.bind(cameraChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(cameraManager.isNull or isConnecting or isCapturing)
        connectButton.textProperty().bind(cameraManager.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        cameraManager.isConnected.on { connectButton.styleClass.toggle("text-red-700", "text-blue-grey-700") }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        menu.items
            .filter { it.userData == "BIND_TO_SELECTED_CAMERA" }
            .forEach { it.disableProperty().bind(isNotConnectedOrCapturing) }

        coolerToggleSwitch.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.hasCooler)
        cameraManager.isCoolerOn.on(coolerToggleSwitch::setSelected)
        coolerToggleSwitch.selectedProperty().on { cameraManager.get().cooler(it) }

        dewHeaterToggleSwitch.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.hasDewHeater)
        dewHeaterToggleSwitch.selectedProperty().bind(cameraManager.isDewHeaterOn)
        // TODO: dewHeaterToggleSwitch.selectedProperty().on { cameraManager.get().dewHeater(it) }

        temperatureLabel.textProperty().bind(cameraManager.temperature.asString(Locale.ENGLISH, "Temperature (%.1f °C)"))
        temperatureSetpointSpinner.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.canSetTemperature)
        temperatureSetpointButton.disableProperty().bind(temperatureSetpointSpinner.disableProperty())

        exposureSpinner.disableProperty().bind(isNotConnectedOrCapturing)

        exposureUnitToggleGroup
            .toggles.forEach { (it as RadioButton).disableProperty().bind(exposureSpinner.disableProperty()) }

        exposureModeToggleGroup
            .toggles.forEach { (it as RadioButton).disableProperty().bind(exposureSpinner.disableProperty()) }
        val fixed = exposureModeToggleGroup
            .toggles.first { it.userData == "FIXED" } as RadioButton
        val continuous = exposureModeToggleGroup
            .toggles.first { it.userData == "CONTINUOUS" } as RadioButton

        exposureDelaySpinner.disableProperty()
            .bind((fixed.disableProperty() and continuous.disableProperty()) or (!fixed.selectedProperty() and !continuous.selectedProperty()))

        exposureCountSpinner.disableProperty().bind(fixed.disableProperty() or !fixed.selectedProperty())

        subFrameToggleSwitch.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.canSubFrame)
        fullsizeButton.disableProperty().bind(subFrameToggleSwitch.disableProperty() or !subFrameToggleSwitch.selectedProperty())

        frameXSpinner.disableProperty().bind(fullsizeButton.disableProperty())
        frameYSpinner.disableProperty().bind(frameXSpinner.disableProperty())
        frameWidthSpinner.disableProperty().bind(frameXSpinner.disableProperty())
        frameHeightSpinner.disableProperty().bind(frameXSpinner.disableProperty())

        binXSpinner.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.canBin)
        binYSpinner.disableProperty().bind(binXSpinner.disableProperty())

        gainSpinner.disableProperty().bind(isNotConnectedOrCapturing)

        offsetSpinner.disableProperty().bind(isNotConnectedOrCapturing)

        frameTypeChoiceBox.disableProperty().bind(isNotConnectedOrCapturing)

        frameFormatChoiceBox.disableProperty().bind(isNotConnectedOrCapturing)
        frameFormatChoiceBox.itemsProperty().bind(cameraManager.frameFormats)

        val invalidExposure = exposureSpinner.valueFactory.valueProperty().isEqualTo(0.0)
        startCaptureButton.disableProperty().bind(isNotConnectedOrCapturing or invalidExposure)
        abortCaptureButton.disableProperty().bind(isNotConnected or !isCapturing or invalidExposure or !cameraManager.canAbort)

        exposureSpinner.userData = TimeUnit.MICROSECONDS

        cameraManager.loadPreferences(null)

        xProperty().on { cameraManager.saveScreenLocation(it, y) }
        yProperty().on { cameraManager.saveScreenLocation(x, it) }
    }

    override fun onStart() {
        cameraManager.loadPreferences()
    }

    override fun onStop() {
        cameraManager.close()
    }

    var exposureUnit
        get() = TimeUnit.valueOf(exposureUnitToggleGroup.selectedToggle.userData as String)
        set(value) {
            exposureUnitToggleGroup.toggles
                .forEach { (it as RadioButton).isSelected = it.userData == value.name }
            exposureSpinner.userData = value
        }

    var exposure
        get() = exposureSpinner.value.toLong()
        set(value) {
            exposureSpinner.valueFactory.value = value.toDouble()
        }

    var exposureMin
        get() = (exposureSpinner.valueFactory as DoubleSpinnerValueFactory).min.toLong()
        set(value) {
            (exposureSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var exposureMax
        get() = (exposureSpinner.valueFactory as DoubleSpinnerValueFactory).max.toLong()
        set(value) {
            (exposureSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    val exposureInMicros
        get() = exposureUnit.toMicros(exposure)

    var temperatureSetpoint
        get() = temperatureSetpointSpinner.value!!
        set(value) {
            temperatureSetpointSpinner.valueFactory.value = value
        }

    var exposureCount
        get() = exposureCountSpinner.value!!.toInt()
        set(value) {
            exposureCountSpinner.valueFactory.value = value.toDouble()
        }

    var exposureMode
        get() = ExposureMode.valueOf(exposureModeToggleGroup.selectedToggle.userData as String)
        set(value) {
            exposureModeToggleGroup
                .toggles.forEach { (it as RadioButton).isSelected = it.userData == value.name }
        }

    var exposureDelay
        get() = exposureDelaySpinner.value!!.toLong()
        set(value) {
            exposureDelaySpinner.valueFactory.value = value.toDouble()
        }

    var isSubFrame
        get() = subFrameToggleSwitch.isSelected
        set(value) {
            subFrameToggleSwitch.isSelected = value
        }

    var frameX
        get() = frameXSpinner.value.toInt()
        set(value) {
            frameXSpinner.valueFactory.value = value.toDouble()
        }

    var frameMinX
        get() = (frameXSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()
        set(value) {
            (frameXSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var frameMaxX
        get() = (frameXSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (frameXSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var frameY
        get() = frameYSpinner.value.toInt()
        set(value) {
            frameYSpinner.valueFactory.value = value.toDouble()
        }

    var frameMinY
        get() = (frameYSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()
        set(value) {
            (frameYSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var frameMaxY
        get() = (frameYSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (frameYSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var frameWidth
        get() = frameWidthSpinner.value.toInt()
        set(value) {
            frameWidthSpinner.valueFactory.value = value.toDouble()
        }

    var frameMinWidth
        get() = (frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()
        set(value) {
            (frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var frameMaxWidth
        get() = (frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var frameHeight
        get() = frameHeightSpinner.value.toInt()
        set(value) {
            frameHeightSpinner.valueFactory.value = value.toDouble()
        }

    var frameMinHeight
        get() = (frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()
        set(value) {
            (frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var frameMaxHeight
        get() = (frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var frameType
        get() = frameTypeChoiceBox.value!!
        set(value) {
            frameTypeChoiceBox.value = value
        }

    var frameFormat: String?
        get() = frameFormatChoiceBox.value
        set(value) {
            frameFormatChoiceBox.value = value
        }

    var binX
        get() = binXSpinner.value.toInt()
        set(value) {
            binXSpinner.valueFactory.value = value.toDouble()
        }

    var maxBinX
        get() = (binXSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (binXSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var binY
        get() = binYSpinner.value.toInt()
        set(value) {
            binYSpinner.valueFactory.value = value.toDouble()
        }

    var maxBinY
        get() = (binYSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (binYSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var gain
        get() = gainSpinner.value.toInt()
        set(value) {
            gainSpinner.valueFactory.value = value.toDouble()
        }

    var gainMin
        get() = (gainSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()
        set(value) {
            (gainSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var gainMax
        get() = (gainSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (gainSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var offset
        get() = offsetSpinner.value.toInt()
        set(value) {
            offsetSpinner.valueFactory.value = value.toDouble()
        }

    var offsetMin
        get() = (offsetSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()
        set(value) {
            (offsetSpinner.valueFactory as DoubleSpinnerValueFactory).min = value.toDouble()
        }

    var offsetMax
        get() = (offsetSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()
        set(value) {
            (offsetSpinner.valueFactory as DoubleSpinnerValueFactory).max = value.toDouble()
        }

    var status
        get() = statusLabel.text!!
        set(value) {
            statusLabel.text = value
        }

    var isNewSubFolderAtNoon
        get() = newSubFolderAtNoonMenuItem.isSelected
        set(value) {
            newSubFolderAtNoonMenuItem.isSelected = value
            newSubFolderAtMidnightMenuItem.isSelected = !value
        }

    var isAutoSaveAllExposures
        get() = autoSaveAllExposuresMenuItem.isSelected
        set(value) {
            autoSaveAllExposuresMenuItem.isSelected = value
            autoSaveAllExposuresIcon.isVisible = value
            autoSaveAllExposuresIcon.isManaged = value
        }

    var isAutoSubFolder
        get() = autoSubFolderMenuItem.isSelected
        set(value) {
            autoSubFolderMenuItem.isSelected = value
            autoSubFolderIcon.isVisible = value
            autoSubFolderIcon.isManaged = value
        }

    var imageSavePath
        get() = imageSavePathLabel.text!!
        set(value) {
            imageSavePathLabel.text = value
        }

    @FXML
    private fun connect() {
        cameraManager.connect()
    }

    @FXML
    private fun openMenu(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY) {
            menu.show(event.source as Node, event.screenX, event.screenY)
            event.consume()
        }
    }

    @FXML
    private fun openINDI() {
        cameraManager.openINDIPanelControl()
    }

    @FXML
    private fun chooseImageSavePath() {
        cameraManager.chooseImageSavePath()
    }

    @FXML
    private fun openImageSavePathInFiles(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
            cameraManager.openImageSavePathInFiles()
        }
    }

    @FXML
    private fun fullsize() {
        if (subFrameToggleSwitch.isSelected) {
            cameraManager.fullsize()
        }
    }

    @FXML
    private fun updateExposureUnit(event: ActionEvent) {
        val radio = event.source as RadioButton
        val timeUnit = TimeUnit.valueOf(radio.userData as String)
        cameraManager.updateExposureUnit(exposureSpinner.userData as TimeUnit, timeUnit, exposure)
    }

    @FXML
    private fun toggleAutoSaveAllExposures() {
        cameraManager.autoSaveAllExposures(autoSaveAllExposuresMenuItem.isSelected)
    }

    @FXML
    private fun toggleAutoSubFolder() {
        cameraManager.autoSubFolder(autoSubFolderMenuItem.isSelected)
    }

    @FXML
    private fun chooseNewSubFolderAt(event: ActionEvent) {
        val menuItem = event.source as CheckMenuItem
        val mode = AutoSubFolderMode.valueOf(menuItem.userData as String)
        cameraManager.chooseNewSubFolderAt(mode)
    }

    @FXML
    private fun applyTemperatureSetpoint() {
        cameraManager.applyTemperatureSetpoint(temperatureSetpointSpinner.value)
    }

    @FXML
    @Synchronized
    private fun startCapture() {
        cameraManager.startCapture()
    }

    @FXML
    private fun abortCapture() {
        cameraManager.abortCapture()
    }

    companion object {

        @Volatile private var window: CameraWindow? = null

        @JvmStatic
        fun open() {
            if (window == null) window = CameraWindow()
            window!!.open(bringToFront = true)
        }
    }
}
