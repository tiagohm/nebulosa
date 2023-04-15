package nebulosa.desktop.gui.camera

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.util.StringConverter
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.LabeledPane
import nebulosa.desktop.gui.control.MaterialIcon
import nebulosa.desktop.gui.control.SwitchSegmentedButton
import nebulosa.desktop.gui.control.TwoStateButton
import nebulosa.desktop.logic.and
import nebulosa.desktop.logic.camera.CameraManager
import nebulosa.desktop.logic.isNull
import nebulosa.desktop.logic.on
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.camera.AutoSubFolderMode
import nebulosa.desktop.view.camera.CameraView
import nebulosa.desktop.view.camera.ExposureMode
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.controlsfx.control.SegmentedButton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Component
class CameraWindow : AbstractWindow("Camera", "camera"), CameraView {

    @Lazy @Autowired private lateinit var cameraManager: CameraManager

    @FXML private lateinit var cameraChoiceBox: ChoiceBox<Camera>
    @FXML private lateinit var connectButton: TwoStateButton
    @FXML private lateinit var openINDIButton: Button
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var autoSaveAllExposuresMenuItem: CheckMenuItem
    @FXML private lateinit var autoSubFolderMenuItem: CheckMenuItem
    @FXML private lateinit var newSubFolderAtNoonMenuItem: CheckMenuItem
    @FXML private lateinit var newSubFolderAtMidnightMenuItem: CheckMenuItem
    @FXML private lateinit var autoSaveAllExposuresIcon: MaterialIcon
    @FXML private lateinit var autoSubFolderIcon: MaterialIcon
    @FXML private lateinit var imageSavePathIcon: MaterialIcon
    @FXML private lateinit var coolerPowerLabel: LabeledPane
    @FXML private lateinit var coolerSwitch: SwitchSegmentedButton
    @FXML private lateinit var dewHeaterSwitch: SwitchSegmentedButton
    @FXML private lateinit var temperatureLabel: LabeledPane
    @FXML private lateinit var temperatureSetpointSpinner: Spinner<Double>
    @FXML private lateinit var temperatureSetpointButton: Button
    @FXML private lateinit var exposureSpinner: Spinner<Double>
    @FXML private lateinit var exposureUnitSegmentedButton: SegmentedButton
    @FXML private lateinit var exposureModeSegmentedButton: SegmentedButton
    @FXML private lateinit var exposureDelaySpinner: Spinner<Double>
    @FXML private lateinit var exposureCountSpinner: Spinner<Double>
    @FXML private lateinit var subFrameSwitch: SwitchSegmentedButton
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
    @FXML private lateinit var statusIcon: MaterialIcon

    init {
        title = "Camera"
        resizable = false
    }

    override fun onCreate() {
        val isNotConnected = !cameraManager.connectedProperty
        val isConnecting = cameraManager.connectingProperty
        val isCapturing = cameraManager.capturingProperty
        val isNotConnectedOrCapturing = isNotConnected or isCapturing

        cameraManager.initialize()

        cameraChoiceBox.converter = CameraStringConverter
        cameraChoiceBox.disableProperty().bind(isConnecting or isCapturing)
        cameraChoiceBox.itemsProperty().bind(cameraManager.cameras)
        cameraManager.bind(cameraChoiceBox.selectionModel.selectedItemProperty())

        connectButton.disableProperty().bind(cameraManager.isNull() or isConnecting or isCapturing)
        cameraManager.connectedProperty.on { connectButton.state = it }

        openINDIButton.disableProperty().bind(connectButton.disableProperty())

        menu.items
            .filter { it.userData == "BIND_TO_SELECTED_CAMERA" }
            .forEach { it.disableProperty().bind(isNotConnectedOrCapturing) }

        coolerPowerLabel.textProperty.bind(cameraManager.coolerPowerProperty.asString(Locale.ENGLISH, "Cooler (%.1f %%)"))
        coolerSwitch.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.hasCoolerProperty)
        cameraManager.coolerProperty.on { coolerSwitch.state = it }
        coolerSwitch.stateProperty.on { cameraManager.get().cooler(it) }

        dewHeaterSwitch.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.hasDewHeaterProperty)
        cameraManager.dewHeaterProperty.on { dewHeaterSwitch.state = it }
        dewHeaterSwitch.stateProperty.on { cameraManager.get().dewHeater(it) }

        temperatureLabel.textProperty.bind(cameraManager.temperatureProperty.asString(Locale.ENGLISH, "Temperature (%.1f °C)"))
        temperatureSetpointSpinner.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.canSetTemperatureProperty)
        temperatureSetpointButton.disableProperty().bind(temperatureSetpointSpinner.disableProperty())

        exposureSpinner.disableProperty().bind(isNotConnectedOrCapturing)

        exposureUnitSegmentedButton.disableProperty().bind(exposureSpinner.disableProperty())
        exposureUnitSegmentedButton.toggleGroup.selectedToggleProperty().on { updateExposureUnit(it!!) }

        exposureModeSegmentedButton.disableProperty().bind(exposureSpinner.disableProperty())

        val fixed = exposureModeSegmentedButton.buttons.first { it.userData == "FIXED" } as ToggleButton
        val loop = exposureModeSegmentedButton.buttons.first { it.userData == "LOOP" } as ToggleButton

        exposureDelaySpinner.disableProperty()
            .bind((fixed.disableProperty() and loop.disableProperty()) or (!fixed.selectedProperty() and !loop.selectedProperty()))

        exposureCountSpinner.disableProperty().bind(fixed.disableProperty() or !fixed.selectedProperty())

        subFrameSwitch.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.canSubFrameProperty)
        fullsizeButton.disableProperty().bind(subFrameSwitch.disableProperty() or !subFrameSwitch.stateProperty)

        frameXSpinner.disableProperty().bind(fullsizeButton.disableProperty())
        frameYSpinner.disableProperty().bind(frameXSpinner.disableProperty())
        frameWidthSpinner.disableProperty().bind(frameXSpinner.disableProperty())
        frameHeightSpinner.disableProperty().bind(frameXSpinner.disableProperty())

        binXSpinner.disableProperty().bind(isNotConnectedOrCapturing or !cameraManager.canBinProperty)
        binXSpinner.valueProperty().on { binYSpinner.valueFactory.value = it }

        // binYSpinner.disableProperty().bind(binXSpinner.disableProperty())
        binYSpinner.disableProperty().set(true)

        gainSpinner.disableProperty().bind(isNotConnectedOrCapturing)

        offsetSpinner.disableProperty().bind(isNotConnectedOrCapturing)

        frameTypeChoiceBox.disableProperty().bind(isNotConnectedOrCapturing)

        frameFormatChoiceBox.disableProperty().bind(isNotConnectedOrCapturing)
        frameFormatChoiceBox.itemsProperty().bind(cameraManager.frameFormatsProperty)

        val invalidExposure = exposureSpinner.valueFactory.valueProperty().isEqualTo(0.0)
        startCaptureButton.disableProperty().bind(isNotConnectedOrCapturing or invalidExposure)
        abortCaptureButton.disableProperty().bind(isNotConnected or !isCapturing or invalidExposure or !cameraManager.canAbortProperty)

        exposureSpinner.userData = TimeUnit.MICROSECONDS

        cameraManager.loadPreferences(null)
    }

    override fun onStart() {
        cameraManager.loadPreferences()
    }

    override fun onStop() {
        cameraManager.savePreferences()
    }

    override val exposureUnit
        get() = TimeUnit.valueOf(exposureUnitSegmentedButton.toggleGroup.selectedToggle.userData as String)

    override val exposure
        get() = exposureSpinner.value.toLong()

    override fun updateExposure(exposure: Long, unit: TimeUnit) {
        exposureSpinner.valueFactory.value = max(1.0, exposure.toDouble())
        val button = exposureUnitSegmentedButton.buttons.first { it.userData == unit.name }
        // exposureUnitSegmentedButton.buttons.forEach { it.isSelected = it === button }
        // exposureUnitSegmentedButton.toggleGroup.selectToggle(button)
        button.isSelected = true
        exposureSpinner.userData = unit
    }

    override val exposureMin
        get() = (exposureSpinner.valueFactory as DoubleSpinnerValueFactory).min.toLong()

    override val exposureMax
        get() = (exposureSpinner.valueFactory as DoubleSpinnerValueFactory).max.toLong()

    override fun updateExposureMinMax(exposureMin: Long, exposureMax: Long) {
        with(exposureSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = exposureMax.toDouble()
            min = exposureMin.toDouble()
        }
    }

    override var temperatureSetpoint
        get() = temperatureSetpointSpinner.value!!
        set(value) {
            temperatureSetpointSpinner.valueFactory.value = value
        }

    override var exposureCount
        get() = exposureCountSpinner.value!!.toInt()
        set(value) {
            exposureCountSpinner.valueFactory.value = value.toDouble()
        }

    override var exposureMode
        get() = ExposureMode.valueOf(exposureModeSegmentedButton.toggleGroup.selectedToggle.userData as String)
        set(value) {
            val button = exposureModeSegmentedButton.buttons.first { it.userData == value.name }
            // exposureModeSegmentedButton.buttons.forEach { it.isSelected = it === button }
            // exposureModeSegmentedButton.toggleGroup.selectToggle(button)
            button.isSelected = true
        }

    override var exposureDelay
        get() = exposureDelaySpinner.value!!.toLong()
        set(value) {
            exposureDelaySpinner.valueFactory.value = value.toDouble()
        }

    override var isSubFrame
        get() = subFrameSwitch.state
        set(value) {
            subFrameSwitch.state = value
        }

    override val frameX
        get() = frameXSpinner.value.toInt()

    override val frameMinX
        get() = (frameXSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()

    override val frameMaxX
        get() = (frameXSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override val frameY
        get() = frameYSpinner.value.toInt()

    override val frameMinY
        get() = (frameYSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()

    override val frameMaxY
        get() = (frameYSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override val frameWidth
        get() = frameWidthSpinner.value.toInt()

    override val frameMinWidth
        get() = (frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()

    override val frameMaxWidth
        get() = (frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override val frameHeight
        get() = frameHeightSpinner.value.toInt()

    override val frameMinHeight
        get() = (frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()

    override val frameMaxHeight
        get() = (frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override fun updateFrameMinMax(
        minX: Int, maxX: Int, minY: Int, maxY: Int,
        minWidth: Int, maxWidth: Int, minHeight: Int, maxHeight: Int,
    ) {
        with(frameXSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = maxX.toDouble()
            min = minX.toDouble()
        }
        with(frameYSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = maxY.toDouble()
            min = minY.toDouble()
        }
        with(frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = maxWidth.toDouble()
            min = minWidth.toDouble()
        }
        with(frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = maxHeight.toDouble()
            min = minHeight.toDouble()
        }
    }

    override fun updateFrame(x: Int, y: Int, width: Int, height: Int) {
        with(frameXSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = x.toDouble()
        }
        with(frameYSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = y.toDouble()
        }
        with(frameWidthSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = width.toDouble()
        }
        with(frameHeightSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = height.toDouble()
        }
    }

    override var frameType
        get() = frameTypeChoiceBox.value!!
        set(value) {
            frameTypeChoiceBox.value = value
        }

    override var frameFormat: String?
        get() = frameFormatChoiceBox.value
        set(value) {
            frameFormatChoiceBox.value = value
        }

    override val binX
        get() = binXSpinner.value.toInt()

    override val maxBinX
        get() = (binXSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override val binY
        get() = binYSpinner.value.toInt()

    override val maxBinY
        get() = (binYSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override fun updateMaxBin(binX: Int, binY: Int) {
        with(binXSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = binX.toDouble()
        }
        with(binYSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = binY.toDouble()
        }
    }

    override fun updateBin(x: Int, y: Int) {
        with(binXSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = x.toDouble()
        }
        with(binYSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = y.toDouble()
        }
    }

    override var gain
        get() = gainSpinner.value.toInt()
        set(value) {
            gainSpinner.valueFactory.value = value.toDouble()
        }

    override val gainMin
        get() = (gainSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()

    override val gainMax
        get() = (gainSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override fun updateGainMinMax(gainMin: Int, gainMax: Int) {
        with(gainSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = gainMax.toDouble()
            min = gainMin.toDouble()
        }
    }

    override var offset
        get() = offsetSpinner.value.toInt()
        set(value) {
            offsetSpinner.valueFactory.value = value.toDouble()
        }

    override val offsetMin
        get() = (offsetSpinner.valueFactory as DoubleSpinnerValueFactory).min.toInt()

    override val offsetMax
        get() = (offsetSpinner.valueFactory as DoubleSpinnerValueFactory).max.toInt()

    override fun updateOffsetMinMax(offsetMin: Int, offsetMax: Int) {
        with(offsetSpinner.valueFactory as DoubleSpinnerValueFactory) {
            max = offsetMax.toDouble()
            min = offsetMin.toDouble()
        }
    }

    override fun updateGainAndOffset(gain: Int, offset: Int) {
        with(gainSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = gain.toDouble()
        }
        with(offsetSpinner.valueFactory as DoubleSpinnerValueFactory) {
            value = offset.toDouble()
        }
    }

    override fun updateStatus(text: String) {
        statusIcon.text = text
    }

    override var autoSubFolderMode
        get() = if (newSubFolderAtMidnightMenuItem.isSelected) AutoSubFolderMode.MIDNIGHT else AutoSubFolderMode.NOON
        set(value) {
            newSubFolderAtNoonMenuItem.isSelected = value == AutoSubFolderMode.NOON
            newSubFolderAtMidnightMenuItem.isSelected = value == AutoSubFolderMode.MIDNIGHT
        }

    override var isAutoSaveAllExposures
        get() = autoSaveAllExposuresMenuItem.isSelected
        set(value) {
            autoSaveAllExposuresMenuItem.isSelected = value
            autoSaveAllExposuresIcon.isVisible = value
            autoSaveAllExposuresIcon.isManaged = value
        }

    override var isAutoSubFolder
        get() = autoSubFolderMenuItem.isSelected
        set(value) {
            autoSubFolderMenuItem.isSelected = value
            autoSubFolderIcon.isVisible = value
            autoSubFolderIcon.isManaged = value
        }

    override var imageSavePath
        get() = imageSavePathIcon.text
        set(value) {
            imageSavePathIcon.text = value
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
    private fun openINDIPanelControl() {
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
        if (subFrameSwitch.state) {
            cameraManager.fullsize()
        }
    }

    private fun updateExposureUnit(toggle: Toggle) {
        val timeUnit = TimeUnit.valueOf(toggle.userData as String)
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
        cameraManager.applyTemperatureSetpoint()
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

    private object CameraStringConverter : StringConverter<Camera>() {

        override fun toString(device: Camera?) = device?.name ?: "No camera selected"

        override fun fromString(text: String?) = null
    }
}
