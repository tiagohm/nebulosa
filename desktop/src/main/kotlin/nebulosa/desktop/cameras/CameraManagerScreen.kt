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
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.cameras.*
import org.controlsfx.control.ToggleSwitch
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.max

class CameraManagerScreen : Screen("CameraManager", "nebulosa-camera-manager") {

    private val equipmentManager by inject<EquipmentManager>()
    private val executor by inject<ExecutorService>()

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
    @FXML private lateinit var progress: Label

    private val connecting = SimpleBooleanProperty(false)
    private val capturing = SimpleBooleanProperty(false)
    private val imageViewers = HashSet<ImageViewerScreen>()

    @Volatile private var subscriber: Disposable? = null

    init {
        title = "Camera"
        isResizable = false
    }

    override fun onCreate() {
        val isNotConnected = equipmentManager.selectedCamera.isConnected.not()
        val isNotConnectedOrCapturing = isNotConnected.or(capturing)
        cameras.disableProperty().bind(connecting.or(capturing))
        equipmentManager.selectedCamera.bind(cameras.selectionModel.selectedItemProperty())
        connect.disableProperty().bind(equipmentManager.selectedCamera.isNull.or(connecting).or(capturing))
        cameraMenuIcon.disableProperty().bind(isNotConnectedOrCapturing)
        cooler.disableProperty().bind(isNotConnectedOrCapturing.or(equipmentManager.selectedCamera.hasCooler.not()))
        dewHeater.disableProperty().bind(isNotConnectedOrCapturing.or(equipmentManager.selectedCamera.hasDewHeater.not()))
        temperatureSetpoint.disableProperty().bind(isNotConnectedOrCapturing.or(equipmentManager.selectedCamera.canSetTemperature.not()))
        applyTemperatureSetpoint.disableProperty().bind(temperatureSetpoint.disableProperty())
        exposure.disableProperty().bind(isNotConnectedOrCapturing)
        exposureUnit.toggles.forEach { (it as RadioButton).disableProperty().bind(exposure.disableProperty()) }
        exposureType.toggles.forEach { (it as RadioButton).disableProperty().bind(exposure.disableProperty()) }
        val fixed = exposureType.toggles.first { it.userData == "FIXED" } as RadioButton
        val continuous = exposureType.toggles.first { it.userData == "CONTINUOUS" } as RadioButton
        exposureDelay.disableProperty().bind(
            fixed.disableProperty().and(continuous.disableProperty()).or(fixed.selectedProperty().not().and(continuous.selectedProperty().not()))
        )
        exposureCount.disableProperty().bind(fixed.disableProperty().or(fixed.selectedProperty().not()))
        subframe.disableProperty().bind(isNotConnectedOrCapturing.or(equipmentManager.selectedCamera.canSubFrame.not()))
        fullsize.disableProperty().bind(subframe.disableProperty().or(subframe.selectedProperty().not()))
        x.disableProperty().bind(fullsize.disableProperty())
        y.disableProperty().bind(x.disableProperty())
        width.disableProperty().bind(x.disableProperty())
        height.disableProperty().bind(x.disableProperty())
        binX.disableProperty().bind(isNotConnectedOrCapturing.or(equipmentManager.selectedCamera.canBin.not()))
        binY.disableProperty().bind(binX.disableProperty())
        frameType.disableProperty().bind(isNotConnectedOrCapturing)
        frameFormat.disableProperty().bind(isNotConnectedOrCapturing)
        startCapture.disableProperty().bind(isNotConnectedOrCapturing)
        abortCapture.disableProperty().bind(isNotConnected.or(startCapture.disableProperty().not()))

        cooler.selectedProperty().bind(equipmentManager.selectedCamera.isCoolerOn)
        dewHeater.selectedProperty().bind(equipmentManager.selectedCamera.isDewHeaterOn)
        temperature.textProperty().bind(equipmentManager.selectedCamera.temperature.asString(Locale.ENGLISH, "Temperature (%.1f °C)"))
        frameFormat.itemsProperty().bind(equipmentManager.selectedCamera.frameFormats)

        equipmentManager.selectedCamera.addListener { _, _, value ->
            title = "Camera | ${value.name}"

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
            .filter { it is DeviceEvent<*> }
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

        imageViewers.forEach(Screen::close)
        imageViewers.clear()
    }

    override fun onEvent(event: Any) {
        if (event is DeviceEvent<*> && event.device === equipmentManager.selectedCamera.value) {
            when (event) {
                is CameraExposureMinMaxChanged -> Platform.runLater(::updateExposure)
                is CameraFrameChanged -> Platform.runLater(::updateFrame)
                is CameraCanBinChanged -> Platform.runLater(::updateBin)
                is CameraFrameFormatsChanged -> Platform.runLater(::updateFrameFormat)
                is CameraExposureTaskProgress -> {
                    Platform.runLater {
                        capturing.value = event.isCapturing || !event.isFinished

                        progress.text = buildString(128) {
                            val task = event.task

                            if (event.isCapturing) {
                                val exposure = if (task.exposure >= 1000000L) "${task.exposure / 1000000L} s"
                                else if (task.exposure >= 1000L) "${task.exposure / 1000L} ms"
                                else "${task.exposure} µs"

                                append("capturing ")
                                append("%d of %d (%s)".format(task.amount - event.remaining, task.amount, exposure))
                                append(" | ")
                                append("%.1f%%".format(Locale.ENGLISH, event.progress * 100.0))
                                append(" | ")
                                append("%s".format(Locale.ENGLISH, task.frameType))
                                // TODO: Filter type.
                            } else if (event.isAborted) {
                                append("aborted")
                            } else if (event.isFinished) {
                                append("finished")
                            } else {
                                return@runLater
                            }
                        }
                    }

                    if (event.imagePath != null) {
                        Platform.runLater {
                            val viewer = imageViewers
                                .firstOrNull { it.camera === event.device }
                                ?: ImageViewerScreen(event.device)

                            imageViewers.add(viewer)

                            viewer.open(event.imagePath.toFile())
                        }
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
    @Synchronized
    private fun startCapture() {
        val camera = equipmentManager.selectedCamera.value ?: return

        val timeUnit = exposure.userData as TimeUnit
        val exposureInMicros = TimeUnit.MICROSECONDS.convert(exposure.value.toLong(), timeUnit)

        val amount = exposureType.toggles
            .firstOrNull { it.isSelected }
            ?.let { if (it.userData == "SINGLE") 1 else if (it.userData == "FIXED") exposureCount.value.toInt() else Int.MAX_VALUE }
            ?: 1

        executor.submit(
            CameraExposureTask(
                camera,
                exposureInMicros, amount, exposureDelay.value.toLong(),
                if (subframe.isSelected) x.value.toInt() else camera.minX,
                if (subframe.isSelected) y.value.toInt() else camera.minY,
                if (subframe.isSelected) width.value.toInt() else camera.maxWidth,
                if (subframe.isSelected) height.value.toInt() else camera.maxHeight,
                frameFormat.value, frameType.value,
                binX.value.toInt(), binY.value.toInt(),
            )
        )
    }

    @FXML
    private fun abortCapture() {
        val camera = equipmentManager.selectedCamera.value ?: return
        camera.abortCapture()
    }
}
