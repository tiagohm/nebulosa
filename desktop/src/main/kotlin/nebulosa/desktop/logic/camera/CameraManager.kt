package nebulosa.desktop.logic.camera

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.DirectoryChooser
import nebulosa.desktop.core.ScreenManager
import nebulosa.desktop.gui.camera.AutoSubFolderMode
import nebulosa.desktop.gui.camera.CameraWindow
import nebulosa.desktop.gui.camera.ExposureMode
import nebulosa.desktop.logic.EquipmentController
import nebulosa.desktop.logic.taskexecutor.TaskEvent
import nebulosa.desktop.logic.taskexecutor.TaskStarted
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.cameras.*
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

class CameraManager(private val window: CameraWindow) : CameraProperty() {

    @JvmField val isCapturing = SimpleBooleanProperty()

    private val preferences by inject<Preferences>()
    private val equipmentController by inject<EquipmentController>()
    private val cameraExposureTaskExecutor by inject<CameraExposureTaskExecutor>()
    private val screenManager by inject<ScreenManager>()
    private val appDirectory by inject<Path>(named("app"))
    private val subscribers = arrayOfNulls<Disposable>(1)

    val cameras get() = equipmentController.attachedCameras

    init {
        subscribers[0] = eventBus
            .filterIsInstance<TaskEvent> {
                it.task is CameraExposureTask
                        && (it.task as CameraExposureTask).camera === value
            }
            .subscribe(::onTaskEvent)
    }

    private fun onTaskEvent(event: TaskEvent) {
        Platform.runLater { isCapturing.set(event is TaskStarted) }
        updateStatus()
    }

    override fun changed(prev: Camera?, new: Camera) {
        super.changed(prev, new)

        savePreferences(prev)
        updateTitle()
        loadPreferences(new)

        equipmentController.selectedCamera.set(new)
    }

    override fun accept(event: DeviceEvent<Camera>) {
        super.accept(event)

        when (event) {
            is CameraExposureAborted,
            is CameraExposureFailed -> updateStatus()
            is CameraExposureProgressChanged -> updateStatus()
            is CameraFrameSaved -> Platform.runLater {
                screenManager
                    .openImageViewer(event.imagePath.toFile(), event.device)
            }
            is CameraExposureMinMaxChanged,
            is CameraFrameChanged,
            is CameraCanBinChanged,
            is CameraGainMinMaxChanged,
            is CameraOffsetMinMaxChanged,
            is CameraFrameFormatsChanged -> loadPreferences()
        }
    }

    fun updateTitle() {
        window.title = "Camera · $name"
    }

    fun updateStatus() {
        val text = buildString(128) {
            val task = cameraExposureTaskExecutor.currentTask

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

        Platform.runLater { window.status = text }
    }

    fun updateFrame() {
        Platform.runLater {
            window.frameMaxX = maxX.get()
            window.frameMinX = minX.get()
            window.frameMaxY = maxY.get()
            window.frameMinY = minY.get()
            window.frameMaxWidth = maxWidth.get()
            window.frameMinWidth = minWidth.get()
            window.frameMaxHeight = maxHeight.get()
            window.frameMinHeight = minHeight.get()
            window.frameWidth = maxWidth.get()
            window.frameHeight = maxHeight.get()
        }
    }

    fun updateMaxBin() {
        Platform.runLater {
            window.maxBinX = maxBinX.get()
            window.maxBinY = maxBinY.get()
        }
    }

    fun updateGainMinMax() {
        Platform.runLater {
            window.gainMax = gainMax.get()
            window.gainMin = gainMin.get()
        }
    }

    fun updateOffsetMinMax() {
        Platform.runLater {
            window.offsetMax = offsetMax.get()
            window.offsetMin = offsetMin.get()
        }
    }

    fun connect() {
        if (isConnected.get()) value.disconnect()
        else value.connect()
    }

    fun fullsize() {
        Platform.runLater {
            window.frameX = minX.get()
            window.frameY = minY.get()
            window.frameWidth = maxWidth.get()
            window.frameHeight = maxHeight.get()
        }
    }

    fun openINDIPanelControl() {
        screenManager.openINDIPanelControl(value)
    }

    fun updateExposureMinMax() {
        val min = window.exposureUnit.convert(exposureMin.value, TimeUnit.MICROSECONDS)
        val max = window.exposureUnit.convert(exposureMax.value, TimeUnit.MICROSECONDS)

        Platform.runLater {
            window.exposureMax = max
            window.exposureMin = min
        }
    }

    fun updateExposureUnit(from: TimeUnit, to: TimeUnit, exposure: Long) {
        val min = to.convert(exposureMin.value, TimeUnit.MICROSECONDS)
        val max = to.convert(exposureMax.value, TimeUnit.MICROSECONDS)
        val value = to.convert(exposure, from)

        Platform.runLater {
            window.exposureMax = max
            window.exposureMin = min
            window.exposure = value
            window.exposureUnit = to
        }
    }

    fun autoSaveAllExposures(enable: Boolean) {
        preferences.bool("camera.$name.autoSaveAllExposures", enable)
        Platform.runLater { window.isAutoSaveAllExposures = enable }
    }

    fun autoSubFolder(enable: Boolean) {
        preferences.bool("camera.$name.autoSubFolder", enable)
        Platform.runLater { window.isAutoSubFolder = enable }
    }

    fun chooseNewSubFolderAt(mode: AutoSubFolderMode) {
        preferences.enum("camera.$name.newSubFolderAt", mode)
        Platform.runLater { window.isNewSubFolderAtNoon = mode == AutoSubFolderMode.NOON }
    }

    fun applyTemperatureSetpoint(temperature: Double) {
        value?.temperature(temperature) ?: return
        preferences.double("camera.$name.temperatureSetpoint", temperature)
    }

    fun chooseImageSavePath() {
        with(DirectoryChooser()) {
            title = "Open Image Save Path"

            val prevImageSavePath = preferences.string("camera.$name.imageSavePath")
            if (!prevImageSavePath.isNullOrBlank()) initialDirectory = File(prevImageSavePath)

            val file = showDialog(window) ?: return

            preferences.string("camera.$name.imageSavePath", "$file")
            Platform.runLater { window.imageSavePath = "$file" }
        }
    }

    fun startCapture() {
        val amount = when (window.exposureMode) {
            ExposureMode.SINGLE -> 1
            ExposureMode.FIXED -> window.exposureCount
            else -> Int.MAX_VALUE
        }

        if (cameraExposureTaskExecutor.currentTask != null) return

        val task = CameraExposureTask(
            value,
            window.exposureInMicros, amount, window.exposureDelay,
            if (window.isSubFrame) window.frameX else minX.get(),
            if (window.isSubFrame) window.frameY else minY.get(),
            if (window.isSubFrame) window.frameWidth else maxWidth.get(),
            if (window.isSubFrame) window.frameHeight else maxHeight.get(),
            window.frameFormat, window.frameType,
            window.binX, window.binY,
            window.gain, window.offset,
            window.isAutoSaveAllExposures,
            Paths.get(window.imageSavePath),
            if (!window.isAutoSubFolder) AutoSubFolderMode.OFF
            else if (window.isNewSubFolderAtNoon) AutoSubFolderMode.NOON
            else AutoSubFolderMode.MIDNIGHT,
        )

        cameraExposureTaskExecutor.add(task)

        savePreferences()
    }

    fun abortCapture() {
        value?.abortCapture()
    }

    fun saveScreenLocation(x: Double, y: Double) {
        preferences.double("camera.screen.x", x)
        preferences.double("camera.screen.y", y)
    }

    fun savePreferences(camera: Camera? = value) {
        if (camera != null && camera.isConnected) {
            preferences.double("camera.${camera.name}.temperatureSetpoint", window.temperatureSetpoint)
            preferences.enum("camera.${camera.name}.exposureUnit", window.exposureUnit)
            preferences.long("camera.${camera.name}.exposure", window.exposureInMicros)
            preferences.int("camera.${camera.name}.exposureCount", window.exposureCount)
            preferences.enum("camera.${camera.name}.exposureMode", window.exposureMode)
            preferences.long("camera.${camera.name}.exposureDelay", window.exposureDelay)
            preferences.bool("camera.${camera.name}.isSubFrame", window.isSubFrame)
            preferences.int("camera.${camera.name}.frameX", window.frameX)
            preferences.int("camera.${camera.name}.frameY", window.frameY)
            preferences.int("camera.${camera.name}.frameWidth", window.frameWidth)
            preferences.int("camera.${camera.name}.frameHeight", window.frameHeight)
            preferences.enum("camera.${camera.name}.frameType", window.frameType)
            preferences.string("camera.${camera.name}.frameFormat", window.frameFormat)
            preferences.int("camera.${camera.name}.binX", window.binX)
            preferences.int("camera.${camera.name}.binY", window.binY)
            preferences.int("camera.${camera.name}.gain", window.gain)
            preferences.int("camera.${camera.name}.offset", window.offset)
        }
    }

    fun loadPreferences(camera: Camera? = value) {
        if (camera != null) {
            updateMaxBin()
            updateExposureMinMax()
            updateFrame()
            updateGainMinMax()
            updateOffsetMinMax()

            Platform.runLater {
                window.temperatureSetpoint = preferences.double("camera.${camera.name}.temperatureSetpoint") ?: 0.0
                val exposureUnit = preferences.enum("camera.${camera.name}.exposureUnit") ?: TimeUnit.MICROSECONDS
                val exposureTimeInMicros = preferences.long("camera.${camera.name}.exposure") ?: 1L
                window.exposureCount = preferences.int("camera.${camera.name}.exposureCount") ?: 1
                window.exposureMode = preferences.enum<ExposureMode>("camera.${camera.name}.exposureMode") ?: ExposureMode.SINGLE
                window.exposureDelay = preferences.long("camera.${camera.name}.exposureDelay") ?: 100L
                window.isSubFrame = preferences.bool("camera.${camera.name}.isSubFrame")
                window.frameX = preferences.int("camera.${camera.name}.frameX") ?: minX.get()
                window.frameY = preferences.int("camera.${camera.name}.frameY") ?: minY.get()
                window.frameWidth = preferences.int("camera.${camera.name}.frameWidth") ?: maxWidth.get()
                window.frameHeight = preferences.int("camera.${camera.name}.frameHeight") ?: maxHeight.get()
                window.frameType = preferences.enum<FrameType>("camera.${camera.name}.frameType") ?: FrameType.LIGHT
                (preferences.string("camera.${camera.name}.frameFormat") ?: frameFormats.firstOrNull())?.let { window.frameFormat = it }
                window.binX = preferences.int("camera.${camera.name}.binX") ?: 1
                window.binY = preferences.int("camera.${camera.name}.binY") ?: 1
                window.gain = preferences.int("camera.${camera.name}.gain") ?: 0
                window.offset = preferences.int("camera.${camera.name}.offset") ?: 0

                updateExposureUnit(TimeUnit.MICROSECONDS, exposureUnit, exposureTimeInMicros)

                window.isAutoSaveAllExposures = preferences.bool("camera.$name.autoSaveAllExposures")
                window.isAutoSubFolder = preferences.bool("camera.$name.autoSubFolder")
                window.isNewSubFolderAtNoon = preferences.enum<AutoSubFolderMode>("camera.$name.newSubFolderAt") != AutoSubFolderMode.MIDNIGHT
                window.imageSavePath = preferences.string("camera.$name.imageSavePath")
                    ?.ifBlank { null }
                    ?: "$appDirectory/captures/$name"
            }
        } else {
            preferences.double("camera.screen.x")?.let { window.x = it }
            preferences.double("camera.screen.y")?.let { window.y = it }
        }
    }

    override fun close() {
        super.close()

        subscribers.forEach { it?.dispose() }
        subscribers.fill(null)
    }
}
