package nebulosa.desktop.logic.camera

import io.reactivex.rxjava3.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.DirectoryChooser
import nebulosa.desktop.App
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.image.ImageWindow
import nebulosa.desktop.gui.indi.INDIPanelControlWindow
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.TaskEventBus
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.observeOnJavaFX
import nebulosa.desktop.logic.task.TaskEvent
import nebulosa.desktop.logic.util.javaFxThread
import nebulosa.desktop.view.camera.AutoSubFolderMode
import nebulosa.desktop.view.camera.CameraView
import nebulosa.desktop.view.camera.ExposureMode
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.*
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class CameraManager(private val view: CameraView) :
    CameraProperty by App.beanFor<EquipmentManager>().selectedCamera {

    val capturingProperty = SimpleBooleanProperty()

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var equipmentManager: EquipmentManager
    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var cameraExecutorService: ExecutorService
    @Autowired private lateinit var taskEventBus: TaskEventBus

    private val subscribers = arrayOfNulls<Disposable>(1)
    private val imageWindows = hashSetOf<ImageWindow>()
    private val runningTask = AtomicReference<CameraExposureTask>()

    val cameras
        get() = equipmentManager.attachedCameras

    init {
        App.autowireBean(this)

        registerListener(this)

        subscribers[0] = taskEventBus
            .observeOnJavaFX()
            .subscribe(::onTaskEvent)
    }

    override fun onChanged(prev: Camera?, device: Camera) {
        if (prev !== device) savePreferences(prev)

        updateTitle()
        loadPreferences(device)
    }

    private fun onTaskEvent(event: TaskEvent) {
        when (event) {
            is CameraFrameSaved -> imageWindows.add(ImageWindow.open(event.imagePath.toFile(), event.task.camera))
        }
    }

    override fun onDeviceEvent(event: DeviceEvent<*>, device: Camera) {
        when (event) {
            is CameraExposureAborted,
            is CameraExposureFailed -> updateStatus()
            is CameraExposureProgressChanged -> updateStatus()
            is CameraExposureMinMaxChanged,
            is CameraFrameChanged,
            is CameraCanBinChanged,
            is CameraGainMinMaxChanged,
            is CameraOffsetMinMaxChanged,
            is CameraFrameFormatsChanged -> loadPreferences()
        }
    }

    private fun updateTitle() {
        view.title = "Camera · $name"
    }

    fun openINDIPanelControl() {
        INDIPanelControlWindow.open(value)
    }

    fun openImageSavePathInFiles() {
        val imageSavePath = preferences.string("camera.$name.imageSavePath") ?: return
        // TODO: hostServices.showDocument(imageSavePath)
    }

    private fun updateStatus() {
        view.status = buildString(128) {
            val task = runningTask.get()

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
    }

    private fun updateFrame() {
        view.updateFrameMinMax(minX, maxX, minY, maxY, minWidth, maxWidth, minHeight, maxHeight)
        view.updateFrame(0, 0, maxWidth, maxHeight)
    }

    private fun updateMaxBin() {
        view.updateMaxBin(maxBinX, maxBinY)
    }

    private fun updateGainMinMax() {
        view.updateGainMinMax(gainMin, gainMax)
    }

    private fun updateOffsetMinMax() {
        view.updateOffsetMinMax(offsetMin, offsetMax)
    }

    fun fullsize() {
        view.updateFrame(minX, minY, maxWidth, maxHeight)
    }

    private fun updateExposureMinMax() {
        val exposureMax = view.exposureUnit.convert(exposureMin, TimeUnit.MICROSECONDS)
        val exposureMin = view.exposureUnit.convert(exposureMax, TimeUnit.MICROSECONDS)
        view.updateExposureMinMax(exposureMin, exposureMax)
    }

    fun updateExposureUnit(from: TimeUnit, to: TimeUnit, exposure: Long) {
        val exposureMax = to.convert(exposureMax, TimeUnit.MICROSECONDS)
        val exposureMin = to.convert(exposureMin, TimeUnit.MICROSECONDS)
        view.updateExposureMinMax(exposureMin, exposureMax)
        view.updateExposure(to.convert(exposure, from), to)
    }

    fun autoSaveAllExposures(enable: Boolean) {
        preferences.bool("camera.$name.autoSaveAllExposures", enable)
        view.isAutoSaveAllExposures = enable
    }

    fun autoSubFolder(enable: Boolean) {
        preferences.bool("camera.$name.autoSubFolder", enable)
        view.isAutoSubFolder = enable
    }

    fun chooseNewSubFolderAt(mode: AutoSubFolderMode) {
        preferences.enum("camera.$name.newSubFolderAt", mode)
        view.autoSubFolderMode = mode
    }

    fun applyTemperatureSetpoint() {
        val temperature = view.temperatureSetpoint
        value?.temperature(temperature) ?: return
        preferences.double("camera.$name.temperatureSetpoint", temperature)
    }

    fun chooseImageSavePath() {
        with(DirectoryChooser()) {
            title = "Open Image Save Path"

            val imageSavePath = preferences.string("camera.$name.imageSavePath")
            if (!imageSavePath.isNullOrBlank()) initialDirectory = File(imageSavePath)

            val file = showDialog(null) ?: return

            preferences.string("camera.$name.imageSavePath", "$file")
            view.imageSavePath = "$file"
        }
    }

    fun startCapture() {
        if (capturingProperty.get()) return

        val amount = when (view.exposureMode) {
            ExposureMode.SINGLE -> 1
            ExposureMode.FIXED -> view.exposureCount
            else -> Int.MAX_VALUE
        }

        val task = CameraExposureTask(
            value,
            view.exposureInMicros, amount, view.exposureDelay,
            if (view.isSubFrame) view.frameX else minX,
            if (view.isSubFrame) view.frameY else minY,
            if (view.isSubFrame) view.frameWidth else maxWidth,
            if (view.isSubFrame) view.frameHeight else maxHeight,
            view.frameFormat, view.frameType,
            view.binX, view.binY,
            view.gain, view.offset,
            view.isAutoSaveAllExposures,
            Paths.get(view.imageSavePath),
            if (!view.isAutoSubFolder) AutoSubFolderMode.OFF
            else view.autoSubFolderMode,
        )

        runningTask.set(task)
        capturingProperty.set(true)
        updateStatus()

        CompletableFuture
            .supplyAsync(task, cameraExecutorService)
            .thenRun {
                capturingProperty.set(false)
                runningTask.set(null)
                javaFxThread { updateStatus() }
            }

        savePreferences()
    }

    fun abortCapture() {
        value?.abortCapture()
    }

    fun savePreferences(device: Camera? = value) {
        if (device != null && device.connected) {
            preferences.double("camera.${device.name}.temperatureSetpoint", view.temperatureSetpoint)
            preferences.enum("camera.${device.name}.exposureUnit", view.exposureUnit)
            preferences.long("camera.${device.name}.exposure", view.exposureInMicros)
            preferences.int("camera.${device.name}.exposureCount", view.exposureCount)
            preferences.enum("camera.${device.name}.exposureMode", view.exposureMode)
            preferences.long("camera.${device.name}.exposureDelay", view.exposureDelay)
            preferences.bool("camera.${device.name}.isSubFrame", view.isSubFrame)
            preferences.int("camera.${device.name}.frameX", view.frameX)
            preferences.int("camera.${device.name}.frameY", view.frameY)
            preferences.int("camera.${device.name}.frameWidth", view.frameWidth)
            preferences.int("camera.${device.name}.frameHeight", view.frameHeight)
            preferences.enum("camera.${device.name}.frameType", view.frameType)
            preferences.string("camera.${device.name}.frameFormat", view.frameFormat)
            preferences.int("camera.${device.name}.binX", view.binX)
            preferences.int("camera.${device.name}.binY", view.binY)
            preferences.int("camera.${device.name}.gain", view.gain)
            preferences.int("camera.${device.name}.offset", view.offset)
        }

        preferences.double("camera.screen.x", view.x)
        preferences.double("camera.screen.y", view.y)
    }

    fun loadPreferences(device: Camera? = value) {
        if (device != null) {
            updateMaxBin()
            updateExposureMinMax()
            updateFrame()
            updateGainMinMax()
            updateOffsetMinMax()

            view.temperatureSetpoint = preferences.double("camera.${device.name}.temperatureSetpoint") ?: 0.0
            val exposureUnit = preferences.enum("camera.${device.name}.exposureUnit") ?: TimeUnit.MICROSECONDS
            val exposureTimeInMicros = preferences.long("camera.${device.name}.exposure") ?: exposureMin
            view.exposureCount = preferences.int("camera.${device.name}.exposureCount") ?: 1
            view.exposureMode = preferences.enum<ExposureMode>("camera.${device.name}.exposureMode") ?: ExposureMode.SINGLE
            view.exposureDelay = preferences.long("camera.${device.name}.exposureDelay") ?: 100L
            view.isSubFrame = preferences.bool("camera.${device.name}.isSubFrame")
            val frameX = preferences.int("camera.${device.name}.frameX") ?: minX
            val frameY = preferences.int("camera.${device.name}.frameY") ?: minY
            val frameWidth = preferences.int("camera.${device.name}.frameWidth") ?: maxWidth
            val frameHeight = preferences.int("camera.${device.name}.frameHeight") ?: maxHeight
            view.updateFrame(frameX, frameY, frameWidth, frameHeight)
            view.frameType = preferences.enum<FrameType>("camera.${device.name}.frameType") ?: FrameType.LIGHT
            (preferences.string("camera.${device.name}.frameFormat") ?: frameFormats.firstOrNull())?.let { view.frameFormat = it }
            val binX = preferences.int("camera.${device.name}.binX") ?: 1
            val binY = preferences.int("camera.${device.name}.binY") ?: 1
            val gain = preferences.int("camera.${device.name}.gain") ?: 0
            val offset = preferences.int("camera.${device.name}.offset") ?: 0
            view.updateBin(binX, binY)
            view.updateGainAndOffset(gain, offset)

            updateExposureUnit(TimeUnit.MICROSECONDS, exposureUnit, exposureTimeInMicros)

            view.isAutoSaveAllExposures = preferences.bool("camera.${device.name}.autoSaveAllExposures")
            view.isAutoSubFolder = preferences.bool("camera.${device.name}.autoSubFolder")
            view.autoSubFolderMode = preferences.enum<AutoSubFolderMode>("camera.${device.name}.newSubFolderAt") ?: AutoSubFolderMode.NOON
            view.imageSavePath =
                preferences.string("camera.${device.name}.imageSavePath") ?: Paths.get("$appDirectory", "captures", device.name).toString()
        }

        preferences.double("camera.screen.x")?.let { view.x = it }
        preferences.double("camera.screen.y")?.let { view.y = it }
    }

    override fun close() {
        savePreferences()

        imageWindows.forEach(AbstractWindow::close)
        imageWindows.clear()

        unregisterListener(this)
    }
}
