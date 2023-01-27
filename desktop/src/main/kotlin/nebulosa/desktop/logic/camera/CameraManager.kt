package nebulosa.desktop.logic.camera

import io.reactivex.rxjava3.disposables.Disposable
import javafx.application.HostServices
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.DirectoryChooser
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.core.EventBus.Companion.observeOnFXThread
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.camera.AutoSubFolderMode
import nebulosa.desktop.gui.camera.CameraWindow
import nebulosa.desktop.gui.camera.ExposureMode
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.EquipmentManager
import nebulosa.desktop.logic.task.TaskEvent
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.desktop.preferences.Preferences
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.cameras.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

class CameraManager(private val window: CameraWindow) :
    CameraProperty by GlobalContext.get().get<EquipmentManager>().selectedCamera, KoinComponent {

    @JvmField val capturingProperty = SimpleBooleanProperty()

    private val preferences by inject<Preferences>()
    private val eventBus by inject<EventBus>()
    private val equipmentManager by inject<EquipmentManager>()
    private val cameraTaskExecutor by inject<CameraTaskExecutor>()
    private val appDirectory by inject<Path>(named("app"))
    private val hostServices by inject<HostServices>()
    private val subscribers = arrayOfNulls<Disposable>(1)
    private val imageWindows = HashSet<ImageWindow>()

    @JvmField val cameras = equipmentManager.attachedCameras

    init {
        subscribers[0] = eventBus
            .filterIsInstance<TaskEvent> { it.task is CameraTask && (it.task as CameraTask).camera === value }
            .observeOnFXThread()
            .subscribe(::onTaskEvent)
    }

    private fun onTaskEvent(event: TaskEvent) {
        capturingProperty.set(event is TaskStarted)
        updateStatus()
    }

    override fun onChanged(prev: Camera?, device: Camera) {
        if (prev !== device) savePreferences(prev)

        updateTitle()
        loadPreferences(device)

        equipmentManager.selectedCamera.set(device)
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Camera) {
        when (event) {
            is CameraExposureAborted,
            is CameraExposureFailed -> updateStatus()
            is CameraExposureProgressChanged -> updateStatus()
            is CameraFrameSaved -> imageWindows.add(ImageWindow.open(event.imagePath.toFile(), event.device))
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

    fun openINDIPanelControl() {
        INDIPanelControlWindow.open(value)
    }

    fun openImageSavePathInFiles() {
        val imageSavePath = preferences.string("camera.$name.imageSavePath") ?: return
        hostServices.showDocument(imageSavePath)
    }

    fun updateStatus() {
        val text = buildString(128) {
            val task = cameraTaskExecutor.currentTask as? CameraExposureTask

            if (task != null && capturingProperty.get()) {
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

        window.status = text
    }

    fun updateFrame() {
        window.frameMaxX = maxX
        window.frameMinX = minX
        window.frameMaxY = maxY
        window.frameMinY = minY
        window.frameMaxWidth = maxWidth
        window.frameMinWidth = minWidth
        window.frameMaxHeight = maxHeight
        window.frameMinHeight = minHeight
        window.frameWidth = maxWidth
        window.frameHeight = maxHeight
    }

    fun updateMaxBin() {
        window.maxBinX = maxBinX
        window.maxBinY = maxBinY
    }

    fun updateGainMinMax() {
        window.gainMax = gainMax
        window.gainMin = gainMin
    }

    fun updateOffsetMinMax() {
        window.offsetMax = offsetMax
        window.offsetMin = offsetMin
    }

    fun fullsize() {
        window.frameX = minX
        window.frameY = minY
        window.frameWidth = maxWidth
        window.frameHeight = maxHeight
    }

    fun updateExposureMinMax() {
        window.exposureMax = window.exposureUnit.convert(exposureMin, TimeUnit.MICROSECONDS)
        window.exposureMin = window.exposureUnit.convert(exposureMax, TimeUnit.MICROSECONDS)
    }

    fun updateExposureUnit(from: TimeUnit, to: TimeUnit, exposure: Long) {
        window.exposureMax = to.convert(exposureMax, TimeUnit.MICROSECONDS)
        window.exposureMin = to.convert(exposureMin, TimeUnit.MICROSECONDS)
        window.exposure = to.convert(exposure, from)
        window.exposureUnit = to
    }

    fun autoSaveAllExposures(enable: Boolean) {
        preferences.bool("camera.$name.autoSaveAllExposures", enable)
        window.isAutoSaveAllExposures = enable
    }

    fun autoSubFolder(enable: Boolean) {
        preferences.bool("camera.$name.autoSubFolder", enable)
        window.isAutoSubFolder = enable
    }

    fun chooseNewSubFolderAt(mode: AutoSubFolderMode) {
        preferences.enum("camera.$name.newSubFolderAt", mode)
        window.isNewSubFolderAtNoon = mode == AutoSubFolderMode.NOON
    }

    fun applyTemperatureSetpoint() {
        val temperature = window.temperatureSetpoint
        value?.temperature(temperature) ?: return
        preferences.double("camera.$name.temperatureSetpoint", temperature)
    }

    fun chooseImageSavePath() {
        with(DirectoryChooser()) {
            title = "Open Image Save Path"

            val prevImageSavePath = preferences.string("camera.$name.imageSavePath")
            if (!prevImageSavePath.isNullOrBlank()) initialDirectory = File(prevImageSavePath)

            val file = showDialog(null) ?: return

            preferences.string("camera.$name.imageSavePath", "$file")
            window.imageSavePath = "$file"
        }
    }

    fun startCapture() {
        val amount = when (window.exposureMode) {
            ExposureMode.SINGLE -> 1
            ExposureMode.FIXED -> window.exposureCount
            else -> Int.MAX_VALUE
        }

        if (cameraTaskExecutor.currentTask != null) return

        val task = CameraExposureTask(
            value,
            window.exposureInMicros, amount, window.exposureDelay,
            if (window.isSubFrame) window.frameX else minX,
            if (window.isSubFrame) window.frameY else minY,
            if (window.isSubFrame) window.frameWidth else maxWidth,
            if (window.isSubFrame) window.frameHeight else maxHeight,
            window.frameFormat, window.frameType,
            window.binX, window.binY,
            window.gain, window.offset,
            window.isAutoSaveAllExposures,
            Paths.get(window.imageSavePath),
            if (!window.isAutoSubFolder) AutoSubFolderMode.OFF
            else if (window.isNewSubFolderAtNoon) AutoSubFolderMode.NOON
            else AutoSubFolderMode.MIDNIGHT,
        )

        cameraTaskExecutor.add(task)

        savePreferences()
    }

    fun abortCapture() {
        value?.abortCapture()
    }

    fun savePreferences(device: Camera? = value) {
        if (device != null && device.isConnected) {
            preferences.double("camera.${device.name}.temperatureSetpoint", window.temperatureSetpoint)
            preferences.enum("camera.${device.name}.exposureUnit", window.exposureUnit)
            preferences.long("camera.${device.name}.exposure", window.exposureInMicros)
            preferences.int("camera.${device.name}.exposureCount", window.exposureCount)
            preferences.enum("camera.${device.name}.exposureMode", window.exposureMode)
            preferences.long("camera.${device.name}.exposureDelay", window.exposureDelay)
            preferences.bool("camera.${device.name}.isSubFrame", window.isSubFrame)
            preferences.int("camera.${device.name}.frameX", window.frameX)
            preferences.int("camera.${device.name}.frameY", window.frameY)
            preferences.int("camera.${device.name}.frameWidth", window.frameWidth)
            preferences.int("camera.${device.name}.frameHeight", window.frameHeight)
            preferences.enum("camera.${device.name}.frameType", window.frameType)
            preferences.string("camera.${device.name}.frameFormat", window.frameFormat)
            preferences.int("camera.${device.name}.binX", window.binX)
            preferences.int("camera.${device.name}.binY", window.binY)
            preferences.int("camera.${device.name}.gain", window.gain)
            preferences.int("camera.${device.name}.offset", window.offset)
        } else if (device == null) {
            preferences.double("camera.screen.x", window.x)
            preferences.double("camera.screen.y", window.y)
        }
    }

    fun loadPreferences(device: Camera? = value) {
        if (device != null) {
            updateMaxBin()
            updateExposureMinMax()
            updateFrame()
            updateGainMinMax()
            updateOffsetMinMax()

            window.temperatureSetpoint = preferences.double("camera.${device.name}.temperatureSetpoint") ?: 0.0
            val exposureUnit = preferences.enum("camera.${device.name}.exposureUnit") ?: TimeUnit.MICROSECONDS
            val exposureTimeInMicros = preferences.long("camera.${device.name}.exposure") ?: 1L
            window.exposureCount = preferences.int("camera.${device.name}.exposureCount") ?: 1
            window.exposureMode = preferences.enum<ExposureMode>("camera.${device.name}.exposureMode") ?: ExposureMode.SINGLE
            window.exposureDelay = preferences.long("camera.${device.name}.exposureDelay") ?: 100L
            window.isSubFrame = preferences.bool("camera.${device.name}.isSubFrame")
            window.frameX = preferences.int("camera.${device.name}.frameX") ?: minX
            window.frameY = preferences.int("camera.${device.name}.frameY") ?: minY
            window.frameWidth = preferences.int("camera.${device.name}.frameWidth") ?: maxWidth
            window.frameHeight = preferences.int("camera.${device.name}.frameHeight") ?: maxHeight
            window.frameType = preferences.enum<FrameType>("camera.${device.name}.frameType") ?: FrameType.LIGHT
            (preferences.string("camera.${device.name}.frameFormat") ?: frameFormats.firstOrNull())?.let { window.frameFormat = it }
            window.binX = preferences.int("camera.${device.name}.binX") ?: 1
            window.binY = preferences.int("camera.${device.name}.binY") ?: 1
            window.gain = preferences.int("camera.${device.name}.gain") ?: 0
            window.offset = preferences.int("camera.${device.name}.offset") ?: 0

            updateExposureUnit(TimeUnit.MICROSECONDS, exposureUnit, exposureTimeInMicros)

            window.isAutoSaveAllExposures = preferences.bool("camera.${device.name}.autoSaveAllExposures")
            window.isAutoSubFolder = preferences.bool("camera.${device.name}.autoSubFolder")
            window.isNewSubFolderAtNoon = preferences.enum<AutoSubFolderMode>("camera.${device.name}.newSubFolderAt") != AutoSubFolderMode.MIDNIGHT
            window.imageSavePath = preferences.string("camera.${device.name}.imageSavePath") ?: "$appDirectory/captures/${device.name}"
        } else {
            preferences.double("camera.screen.x")?.let { window.x = it }
            preferences.double("camera.screen.y")?.let { window.y = it }
        }
    }

    override fun close() {
        savePreferences(null)
        savePreferences()

        imageWindows.forEach(AbstractWindow::close)
        imageWindows.clear()
    }
}
