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
import nebulosa.desktop.Nebulosa
import nebulosa.desktop.core.beans.*
import nebulosa.desktop.core.scene.MaterialColor
import nebulosa.desktop.core.scene.MaterialIcon
import nebulosa.desktop.core.scene.Screen
import nebulosa.desktop.core.util.DeviceStringConverter
import nebulosa.desktop.equipments.EquipmentManager
import nebulosa.desktop.imageviewer.ImageViewerScreen
import nebulosa.indi.devices.cameras.*
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isWritable
import kotlin.math.max

class CameraManagerScreen : Screen("CameraManager", "nebulosa-camera-manager") {

    private val nebulosa by inject<Nebulosa>()
    private val equipmentManager by inject<EquipmentManager>()

    @FXML private lateinit var cameras: ChoiceBox<Camera>
    @FXML private lateinit var connect: Button
    @FXML private lateinit var openINDI: Button
    @FXML private lateinit var cameraMenuIcon: Label
    @FXML private lateinit var cameraMenu: ContextMenu
    @FXML private lateinit var autoSaveAllExposures: CheckMenuItem
    @FXML private lateinit var autoSubFolder: CheckMenuItem
    @FXML private lateinit var newSubFolderAtNoon: CheckMenuItem
    @FXML private lateinit var newSubFolderAtMidnight: CheckMenuItem
    @FXML private lateinit var autoSaveAllExposuresIndicator: Label
    @FXML private lateinit var autoSubFolderIndicator: Label
    @FXML private lateinit var savePathIndicator: Label
    @FXML private lateinit var cooler: ToggleSwitch
    @FXML private lateinit var dewHeater: ToggleSwitch
    @FXML private lateinit var temperature: Label
    @FXML private lateinit var temperatureSetpoint: Spinner<Double>
    @FXML private lateinit var applyTemperatureSetpoint: Button
    @FXML private lateinit var exposure: Spinner<Double>
    @FXML private lateinit var exposureUnit: ToggleGroup
    @FXML private lateinit var exposureMode: ToggleGroup
    @FXML private lateinit var exposureDelay: Spinner<Double>
    @FXML private lateinit var exposureCount: Spinner<Double>
    @FXML private lateinit var subFrame: ToggleSwitch
    @FXML private lateinit var fullsize: Button
    @FXML private lateinit var frameX: Spinner<Double>
    @FXML private lateinit var frameY: Spinner<Double>
    @FXML private lateinit var frameWidth: Spinner<Double>
    @FXML private lateinit var frameHeight: Spinner<Double>
    @FXML private lateinit var binX: Spinner<Double>
    @FXML private lateinit var binY: Spinner<Double>
    @FXML private lateinit var gain: Spinner<Double>
    @FXML private lateinit var offset: Spinner<Double>
    @FXML private lateinit var frameType: ChoiceBox<FrameType>
    @FXML private lateinit var frameFormat: ChoiceBox<String>
    @FXML private lateinit var startCapture: Button
    @FXML private lateinit var abortCapture: Button
    @FXML private lateinit var status: Label

    private val isCapturing = SimpleBooleanProperty(false)
    private val imageViewers = HashSet<ImageViewerScreen>()

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Camera"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = !equipmentManager.selectedCamera.isConnected
        val isConnecting = equipmentManager.selectedCamera.isConnecting
        val isCapturing = equipmentManager.selectedCamera.isCapturing or this.isCapturing
        val isNotConnectedOrCapturing = isNotConnected or isCapturing

        cameras.converter = DeviceStringConverter()
        cameras.disableProperty().bind(isConnecting or isCapturing)
        cameras.itemsProperty().bind(equipmentManager.attachedCameras)
        equipmentManager.selectedCamera.bind(cameras.selectionModel.selectedItemProperty())

        connect.disableProperty().bind(equipmentManager.selectedCamera.isNull or isConnecting or isCapturing)
        connect.textProperty().bind(equipmentManager.selectedCamera.isConnected.between(MaterialIcon.CLOSE_CIRCLE, MaterialIcon.CONNECTION))
        connect.textFillProperty().bind(equipmentManager.selectedCamera.isConnected.between(MaterialColor.RED_700, MaterialColor.BLUE_GREY_700))

        openINDI.disableProperty().bind(connect.disableProperty())

        cameraMenuIcon.disableProperty().bind(isNotConnectedOrCapturing)

        cooler.disableProperty().bind(isNotConnectedOrCapturing or !equipmentManager.selectedCamera.hasCooler)
        equipmentManager.selectedCamera.isCoolerOn.on(cooler::setSelected)
        cooler.selectedProperty().on { equipmentManager.selectedCamera.get().cooler(it) }

        dewHeater.disableProperty().bind(isNotConnectedOrCapturing or !equipmentManager.selectedCamera.hasDewHeater)
        dewHeater.selectedProperty().bind(equipmentManager.selectedCamera.isDewHeaterOn)
        // TODO: Send dew heater command.

        temperature.textProperty().bind(equipmentManager.selectedCamera.temperature.asString(Locale.ENGLISH, "Temperature (%.1f °C)"))
        temperatureSetpoint.disableProperty().bind(isNotConnectedOrCapturing or !equipmentManager.selectedCamera.canSetTemperature)
        applyTemperatureSetpoint.disableProperty().bind(temperatureSetpoint.disableProperty())

        exposure.disableProperty().bind(isNotConnectedOrCapturing)

        exposureUnit.toggles.forEach { (it as RadioButton).disableProperty().bind(exposure.disableProperty()) }

        exposureMode.toggles.forEach { (it as RadioButton).disableProperty().bind(exposure.disableProperty()) }
        val fixed = exposureMode.toggles.first { it.userData == "FIXED" } as RadioButton
        val continuous = exposureMode.toggles.first { it.userData == "CONTINUOUS" } as RadioButton

        exposureDelay.disableProperty()
            .bind((fixed.disableProperty() and continuous.disableProperty()) or (!fixed.selectedProperty() and !continuous.selectedProperty()))

        exposureCount.disableProperty().bind(fixed.disableProperty() or !fixed.selectedProperty())

        subFrame.disableProperty().bind(isNotConnectedOrCapturing or !equipmentManager.selectedCamera.canSubFrame)
        fullsize.disableProperty().bind(subFrame.disableProperty() or !subFrame.selectedProperty())

        frameX.disableProperty().bind(fullsize.disableProperty())
        frameY.disableProperty().bind(frameX.disableProperty())
        frameWidth.disableProperty().bind(frameX.disableProperty())
        frameHeight.disableProperty().bind(frameX.disableProperty())

        binX.disableProperty().bind(isNotConnectedOrCapturing or !equipmentManager.selectedCamera.canBin)
        binY.disableProperty().bind(binX.disableProperty())

        gain.disableProperty().bind(isNotConnectedOrCapturing)

        offset.disableProperty().bind(isNotConnectedOrCapturing)

        frameType.disableProperty().bind(isNotConnectedOrCapturing)

        frameFormat.disableProperty().bind(isNotConnectedOrCapturing)
        frameFormat.itemsProperty().bind(equipmentManager.selectedCamera.frameFormats)

        startCapture.disableProperty().bind(isNotConnectedOrCapturing)

        abortCapture.disableProperty().bind(isNotConnected or !startCapture.disableProperty() or !equipmentManager.selectedCamera.canAbort)

        equipmentManager.selectedCamera.onTwo { prev, value ->
            title = "Camera · ${value?.name}"

            savePreferences(prev)
            // updateExposure()
            updateFrame()
            updateFrameFormat()
            updateBin()
            updateGain()
            updateOffset()
            loadPreferences(value)
        }

        cameraMenuIcon.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                cameraMenu.show(cameraMenuIcon, it.screenX, it.screenY)
                it.consume()
            }
        }

        exposure.userData = TimeUnit.MICROSECONDS

        preferences.double("cameraManager.screen.x")?.let { x = it }
        preferences.double("cameraManager.screen.y")?.let { y = it }

        xProperty().on { preferences.double("cameraManager.screen.x", it) }
        yProperty().on { preferences.double("cameraManager.screen.y", it) }
    }

    override fun onStart() {
        subscriber = eventBus
            .filterIsInstance<CameraEvent> { it.device === equipmentManager.selectedCamera.get() }
            .subscribe(::onCameraEvent)

        val camera = equipmentManager.selectedCamera.get()

        if (camera !in equipmentManager.attachedCameras) {
            cameras.selectionModel.select(null)
        }
    }

    override fun onStop() {
        subscriber?.dispose()
        subscriber = null

        imageViewers.forEach(Screen::close)
        imageViewers.clear()
    }

    private fun onCameraEvent(event: CameraEvent) {
        when (event) {
            is CameraExposureAborted,
            is CameraExposureFinished,
            is CameraExposureFailed -> Platform.runLater {
                val isFinished = event is CameraExposureAborted || event is CameraExposureFailed
                val isCapturing = equipmentManager.imagingCameraTask?.isCapturing ?: false
                this.isCapturing.set(isCapturing && !isFinished)
                updateTitle()
            }
            is CameraExposureProgressChanged -> Platform.runLater { updateTitle() }
            is CameraFrameSaved -> Platform.runLater {
                screenManager
                    .openImageViewer(event.imagePath.toFile(), event.device)
                    .also(imageViewers::add)
            }
            is CameraExposureMinMaxChanged -> Platform.runLater {
                updateExposure()
                loadPreferences(event.device)
            }
            is CameraFrameChanged -> Platform.runLater {
                updateFrame()
                loadPreferences(event.device)
            }
            is CameraCanBinChanged -> Platform.runLater {
                updateBin()
                loadPreferences(event.device)
            }
            is CameraGainMinMaxChanged -> Platform.runLater {
                updateGain()
                loadPreferences(event.device)
            }
            is CameraOffsetMinMaxChanged -> Platform.runLater {
                updateOffset()
                loadPreferences(event.device)
            }
            is CameraFrameFormatsChanged -> Platform.runLater {
                updateFrameFormat()
                loadPreferences(event.device)
            }
        }
    }

    @FXML
    private fun connect() {
        if (!equipmentManager.selectedCamera.isConnected.get()) {
            equipmentManager.selectedCamera.get().connect()
        } else {
            equipmentManager.selectedCamera.get().disconnect()
        }
    }

    @FXML
    private fun openINDI() {
        val camera = equipmentManager.selectedCamera.get() ?: return
        screenManager.openINDIPanelControl(camera)
    }

    @FXML
    private fun toggleAutoSaveAllExposures() {
        val camera = equipmentManager.selectedCamera.get() ?: return
        preferences.bool("cameraManager.equipment.${camera.name}.autoSaveAllExposures", autoSaveAllExposures.isSelected)
        updateIndicators()
    }

    @FXML
    private fun toggleAutoSubFolder() {
        val camera = equipmentManager.selectedCamera.get() ?: return
        preferences.bool("cameraManager.equipment.${camera.name}.autoSubFolder", autoSubFolder.isSelected)
        updateIndicators()
    }

    @FXML
    private fun openImageSavePath() {
        val camera = equipmentManager.selectedCamera.get() ?: return
        val chooser = DirectoryChooser()
        val initialDirectory = preferences.string("cameraManager.equipment.${camera.name}.imageSavePath")
        if (!initialDirectory.isNullOrBlank()) chooser.initialDirectory = File(initialDirectory)
        chooser.title = "Open Image Save Path"
        val file = chooser.showDialog(this) ?: return
        preferences.string("cameraManager.equipment.${camera.name}.imageSavePath", file.toString())
        updateIndicators()
    }

    @FXML
    private fun openFolderInFiles(event: MouseEvent) {
        if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
            val camera = equipmentManager.selectedCamera.get() ?: return
            val path = cameraImageSavePath(camera)
            nebulosa.hostServices.showDocument(path.toString())
        }
    }

    @FXML
    private fun chooseNewSubFolderAt(event: ActionEvent) {
        val camera = equipmentManager.selectedCamera.get() ?: return
        val menuItem = event.source as CheckMenuItem
        val mode = AutoSubFolderMode.valueOf(menuItem.userData as String)
        menuItem.parentMenu.items.onEach { (it as CheckMenuItem).isSelected = it === menuItem }
        preferences.enum("cameraManager.equipment.${camera.name}.newSubFolderAt", mode)
    }

    @FXML
    private fun applyTemperatureSetpoint() {
        val camera = equipmentManager.selectedCamera.get() ?: return
        camera.temperature(temperatureSetpoint.value)
    }

    @FXML
    private fun updateExposureUnit(event: ActionEvent) {
        val radio = event.source as RadioButton
        val timeUnit = TimeUnit.valueOf(radio.userData as String)
        val prevTimeUnit = exposure.userData as TimeUnit
        updateExposureUnit(prevTimeUnit, timeUnit, exposure.value.toLong())
    }

    private fun updateExposureUnit(from: TimeUnit, to: TimeUnit, exposureValue: Long) {
        val minValue = max(1L, to.convert(equipmentManager.selectedCamera.exposureMin.value, TimeUnit.MICROSECONDS))
        val maxValue = to.convert(equipmentManager.selectedCamera.exposureMax.value, TimeUnit.MICROSECONDS)
        with(exposure.valueFactory as DoubleSpinnerValueFactory) {
            max = maxValue.toDouble()
            min = minValue.toDouble()
            value = to.convert(exposureValue, from).toDouble()
            exposure.userData = to
        }
    }

    private fun loadPreferences(camera: Camera?) {
        if (camera != null) {
            preferences.double("cameraManager.equipment.${camera.name}.temperature")?.also { temperatureSetpoint.valueFactory.value = it }
            val timeUnit = preferences.enum("cameraManager.equipment.${camera.name}.exposureUnit") ?: TimeUnit.MICROSECONDS
            val exposure = preferences.long("cameraManager.equipment.${camera.name}.exposure") ?: camera.exposureMin
            exposureUnit.selectToggle(exposureUnit.toggles.find { it.userData == timeUnit.name })
            updateExposureUnit(TimeUnit.MICROSECONDS, timeUnit, exposure)
            preferences.double("cameraManager.equipment.${camera.name}.amount")?.also { exposureCount.valueFactory.value = it }
            preferences.string("cameraManager.equipment.${camera.name}.exposureMode")
                ?.also { mode -> exposureMode.selectToggle(exposureMode.toggles.first { it.userData == mode }) }
            preferences.double("cameraManager.equipment.${camera.name}.exposureDelay")?.also { exposureDelay.valueFactory.value = it }
            preferences.bool("cameraManager.equipment.${camera.name}.subFrame").also { subFrame.isSelected = it }
            preferences.double("cameraManager.equipment.${camera.name}.x")?.also { frameX.valueFactory.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.y")?.also { frameY.valueFactory.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.width")?.also { frameWidth.valueFactory.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.height")?.also { frameHeight.valueFactory.value = it }
            preferences.enum<FrameType>("cameraManager.equipment.${camera.name}.frameType")?.also { frameType.value = it }
            preferences.string("cameraManager.equipment.${camera.name}.frameFormat")?.also { frameFormat.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.binX")?.also { binX.valueFactory.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.binY")?.also { binY.valueFactory.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.gain")?.also { gain.valueFactory.value = it }
            preferences.double("cameraManager.equipment.${camera.name}.offset")?.also { offset.valueFactory.value = it }
            autoSaveAllExposures.isSelected = preferences.bool("cameraManager.equipment.${camera.name}.autoSaveAllExposures")
            autoSubFolder.isSelected = preferences.bool("cameraManager.equipment.${camera.name}.autoSubFolder")
            val mode = preferences.enum("cameraManager.equipment.${camera.name}.newSubFolderAt") ?: AutoSubFolderMode.NOON
            newSubFolderAtMidnight.isSelected = mode == AutoSubFolderMode.MIDNIGHT
            newSubFolderAtNoon.isSelected = mode == AutoSubFolderMode.NOON
        }

        updateIndicators()
    }

    private fun savePreferences(camera: Camera?) {
        if (camera != null) {
            val timeUnit = exposure.userData as TimeUnit
            preferences.double("cameraManager.equipment.${camera.name}.temperature", temperatureSetpoint.value)
            preferences.enum("cameraManager.equipment.${camera.name}.exposureUnit", timeUnit)
            preferences.long("cameraManager.equipment.${camera.name}.exposure", TimeUnit.MICROSECONDS.convert(exposure.value.toLong(), timeUnit))
            preferences.double("cameraManager.equipment.${camera.name}.amount", exposureCount.value)
            preferences.string("cameraManager.equipment.${camera.name}.exposureMode", exposureMode.selectedToggle.userData as String)
            preferences.double("cameraManager.equipment.${camera.name}.exposureDelay", exposureDelay.value)
            preferences.bool("cameraManager.equipment.${camera.name}.subFrame", subFrame.isSelected)
            preferences.double("cameraManager.equipment.${camera.name}.x", frameX.value)
            preferences.double("cameraManager.equipment.${camera.name}.y", frameY.value)
            preferences.double("cameraManager.equipment.${camera.name}.width", frameWidth.value)
            preferences.double("cameraManager.equipment.${camera.name}.height", frameHeight.value)
            preferences.enum("cameraManager.equipment.${camera.name}.frameType", frameType.value)
            preferences.string("cameraManager.equipment.${camera.name}.frameFormat", frameFormat.value)
            preferences.double("cameraManager.equipment.${camera.name}.binX", binX.value)
            preferences.double("cameraManager.equipment.${camera.name}.binY", binY.value)
            preferences.double("cameraManager.equipment.${camera.name}.gain", gain.value)
            preferences.double("cameraManager.equipment.${camera.name}.offset", offset.value)
        }
    }

    private fun updateExposure() {
        val timeUnit = exposure.userData as TimeUnit
        updateExposureUnit(timeUnit, timeUnit, exposure.value.toLong())
    }

    private fun updateFrame() {
        val camera = equipmentManager.selectedCamera.get() ?: return

        with(frameX.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxX.toDouble()
            min = camera.minX.toDouble()
            if (!subFrame.isSelected) value = min
        }
        with(frameY.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxY.toDouble()
            min = camera.minY.toDouble()
            if (!subFrame.isSelected) value = min
        }
        with(frameWidth.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxWidth.toDouble()
            min = camera.minWidth.toDouble()
            if (!subFrame.isSelected) value = max
        }
        with(frameHeight.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.maxHeight.toDouble()
            min = camera.minHeight.toDouble()
            if (!subFrame.isSelected) value = max
        }
    }

    private fun updateFrameFormat() {
        val selectedFrameFormat = frameFormat.selectionModel.selectedItem

        if (selectedFrameFormat == null || selectedFrameFormat !in equipmentManager.selectedCamera.frameFormats) {
            frameFormat.selectionModel.selectFirst()
        }
    }

    private fun updateBin() {
        val camera = equipmentManager.selectedCamera.get() ?: return

        (binX.valueFactory as DoubleSpinnerValueFactory).max = camera.maxBinX.toDouble()
        (binY.valueFactory as DoubleSpinnerValueFactory).max = camera.maxBinY.toDouble()
    }

    private fun updateGain() {
        val camera = equipmentManager.selectedCamera.get() ?: return

        with(gain.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.gainMax.toDouble()
            min = camera.gainMin.toDouble()
        }
    }

    private fun updateOffset() {
        val camera = equipmentManager.selectedCamera.get() ?: return

        with(offset.valueFactory as DoubleSpinnerValueFactory) {
            max = camera.offsetMax.toDouble()
            min = camera.offsetMin.toDouble()
        }
    }

    private fun updateIndicators() {
        val camera = equipmentManager.selectedCamera.get()

        if (camera != null) {
            val imageSavePath = cameraImageSavePath(camera)
            savePathIndicator.text = imageSavePath.toString()

            val autoSaveAllExposures = preferences.bool("cameraManager.equipment.${camera.name}.autoSaveAllExposures")
            autoSaveAllExposuresIndicator.isVisible = autoSaveAllExposures
            autoSaveAllExposuresIndicator.isManaged = autoSaveAllExposures

            val autoSubFolder = autoSaveAllExposures && preferences.bool("cameraManager.equipment.${camera.name}.autoSubFolder")
            autoSubFolderIndicator.isVisible = autoSubFolder
            autoSubFolderIndicator.isManaged = autoSubFolder
        } else {
            savePathIndicator.text = "-"

            autoSaveAllExposuresIndicator.isVisible = false
            autoSaveAllExposuresIndicator.isManaged = false

            autoSubFolderIndicator.isVisible = false
            autoSubFolderIndicator.isManaged = false
        }
    }

    @FXML
    private fun applyFullsize() {
        val camera = equipmentManager.selectedCamera.get() ?: return

        (frameX.valueFactory as DoubleSpinnerValueFactory).value = camera.minX.toDouble()
        (frameY.valueFactory as DoubleSpinnerValueFactory).value = camera.minY.toDouble()
        (frameWidth.valueFactory as DoubleSpinnerValueFactory).value = camera.maxWidth.toDouble()
        (frameHeight.valueFactory as DoubleSpinnerValueFactory).value = camera.maxHeight.toDouble()
    }

    @FXML
    @Synchronized
    private fun startCapture() {
        if (isCapturing.get()) return

        val camera = equipmentManager.selectedCamera.get() ?: return

        val timeUnit = exposure.userData as TimeUnit
        val exposureInMicros = TimeUnit.MICROSECONDS.convert(exposure.value.toLong(), timeUnit)

        val amount = exposureMode.toggles
            .firstOrNull { it.isSelected }
            ?.let { if (it.userData == "SINGLE") 1 else if (it.userData == "FIXED") exposureCount.value.toInt() else Int.MAX_VALUE }
            ?: 1

        equipmentManager
            .startImagingCapture(
                camera,
                equipmentManager.selectedFilterWheel.value,
                exposureInMicros, amount, exposureDelay.value.toLong(),
                if (subFrame.isSelected) frameX.value.toInt() else camera.minX,
                if (subFrame.isSelected) frameY.value.toInt() else camera.minY,
                if (subFrame.isSelected) frameWidth.value.toInt() else camera.maxWidth,
                if (subFrame.isSelected) frameHeight.value.toInt() else camera.maxHeight,
                frameFormat.value, frameType.value,
                binX.value.toInt(), binY.value.toInt(),
                gain.value.toInt(), offset.value.toInt(),
                preferences.bool("cameraManager.equipment.${camera.name}.autoSaveAllExposures"),
                cameraImageSavePath(camera),
                if (!preferences.bool("cameraManager.equipment.${camera.name}.autoSubFolder")) AutoSubFolderMode.OFF
                else preferences.enum<AutoSubFolderMode>("cameraManager.equipment.${camera.name}.newSubFolderAt") ?: AutoSubFolderMode.NOON,
            ) ?: return

        savePreferences(camera)

        isCapturing.set(true)
    }

    @FXML
    private fun abortCapture() {
        equipmentManager.imagingCameraTask?.cancel(true)
    }

    private fun updateTitle() {
        status.text = buildString(128) {
            val task = equipmentManager.imagingCameraTask

            if (task != null && isCapturing.get()) {
                val exposure = if (task.exposure >= 1000000L) "${task.exposure / 1000000.0} s"
                else if (task.exposure >= 1000L) "${task.exposure / 1000.0} ms"
                else "${task.exposure} µs"

                append("capturing ")
                append("%d of %d (%s)".format(task.amount - task.remaining, task.amount, exposure))
                append(" | ")
                append("%.1f%%".format(Locale.ENGLISH, task.progress * 100.0))
                append(" | ")
                append("%s".format(Locale.ENGLISH, task.frameType))
            } else {
                append("idle")
            }
        }
    }

    private fun cameraImageSavePath(camera: Camera): Path {
        return preferences.string("cameraManager.equipment.${camera.name}.imageSavePath")
            ?.ifBlank { null }
            ?.let(Paths::get)
            ?.takeIf { it.exists() && it.isDirectory() && it.isWritable() }
            ?: Paths.get("${get<Path>(named("app"))}", "captures", camera.name)
    }
}
