package nebulosa.desktop.logic.camera

import javafx.application.HostServices
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.DirectoryChooser
import nebulosa.desktop.helper.runBlockingMain
import nebulosa.desktop.logic.AbstractManager
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.logic.task.TaskEvent
import nebulosa.desktop.logic.task.TaskExecutor
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.desktop.view.View
import nebulosa.desktop.view.camera.AutoSubFolderMode
import nebulosa.desktop.view.camera.CameraView
import nebulosa.desktop.view.camera.ExposureMode
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

@Component
class CameraManager(
    @Autowired internal val view: CameraView,
    @Autowired internal val equipmentManager: EquipmentManager,
) : AbstractManager(), CameraProperty by equipmentManager.selectedCamera {

    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var taskExecutor: TaskExecutor
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory

    private val imageViews = hashSetOf<ImageView>()
    private val runningTask = AtomicReference<CameraExposureTask>()

    val capturingProperty = SimpleBooleanProperty()

    val cameras
        get() = equipmentManager.attachedCameras

    fun initialize() {
        registerListener(this)

        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onTaskEvent(event: TaskEvent): Unit = runBlockingMain {
        when (event) {
            is TaskStarted -> {
                if (event.task === runningTask.get()) {
                    updateStatus()
                }
            }
            is CameraFrameSaved -> {
                val window = imageViewOpener.open(event.image, event.path.toFile(), event.task.camera)
                imageViews.add(window)
            }
        }
    }

    override fun onChanged(prev: Camera?, device: Camera) {
        if (prev !== device) savePreferences(prev)

        updateTitle()
        loadPreferences(device)
    }

    override fun onReset() {}

    override suspend fun onDeviceEvent(event: DeviceEvent<*>, device: Camera) {
        when (event) {
            is CameraExposureAborted,
            is CameraExposureFailed,
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
        indiPanelControlView.show(value)
    }

    fun openImageSavePathInFiles() {
        val imageSavePath = preferenceService.string("camera.$name.imageSavePath") ?: return
        val hostServices = beanFactory.getBean(HostServices::class.java)
        hostServices.showDocument(imageSavePath)
    }

    private fun updateStatus() {
        val text = buildString(64) {
            val task = runningTask.get()

            if (task != null && capturingProperty.get()) {
                append("%d of %s".format(task.amount - task.remainingAmount, if (task.amount >= Int.MAX_VALUE) "∞" else task.amount))
                append(" | ")
                append("%.1fs".format(task.remainingTime / 1000000.0))
                append(" | ")
                append("%.1fs".format(task.elapsedTime / 1000000.0))
                if (task.amount < Int.MAX_VALUE) {
                    append(" of ")
                    append("%.1fs".format(task.totalExposureTime / 1000000.0))
                }
                append(" | ")
                append("%s".format(task.frameType))

                task.filterName?.also {
                    append(" | ")
                    append(it)
                }
            } else {
                append("idle")
            }
        }

        view.updateStatus(text)
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
        preferenceService.bool("camera.$name.autoSaveAllExposures", enable)
        view.isAutoSaveAllExposures = enable
    }

    fun autoSubFolder(enable: Boolean) {
        preferenceService.bool("camera.$name.autoSubFolder", enable)
        view.isAutoSubFolder = enable
    }

    fun chooseNewSubFolderAt(mode: AutoSubFolderMode) {
        preferenceService.enum("camera.$name.newSubFolderAt", mode)
        view.autoSubFolderMode = mode
    }

    fun applyTemperatureSetpoint() {
        val temperature = view.temperatureSetpoint
        value?.temperature(temperature) ?: return
        preferenceService.double("camera.$name.temperatureSetpoint", temperature)
    }

    fun chooseImageSavePath() {
        with(DirectoryChooser()) {
            title = "Open Image Save Path"

            val imageSavePath = preferenceService.string("camera.$name.imageSavePath")
            if (!imageSavePath.isNullOrBlank()) initialDirectory = File(imageSavePath)

            val file = showDialog(null) ?: return

            preferenceService.string("camera.$name.imageSavePath", "$file")
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

        taskExecutor
            .execute(task)
            .whenComplete { _, _ ->
                runBlockingMain {
                    capturingProperty.set(false)
                    runningTask.set(null)
                    updateStatus()
                }
            }

        savePreferences()
    }

    fun abortCapture() {
        runningTask.get()?.abort()
    }

    fun savePreferences(device: Camera? = value) {
        if (!view.initialized) return

        if (device != null && device.connected) {
            preferenceService.double("camera.${device.name}.temperatureSetpoint", view.temperatureSetpoint)
            preferenceService.enum("camera.${device.name}.exposureUnit", view.exposureUnit)
            preferenceService.long("camera.${device.name}.exposure", view.exposureInMicros)
            preferenceService.int("camera.${device.name}.exposureCount", view.exposureCount)
            preferenceService.enum("camera.${device.name}.exposureMode", view.exposureMode)
            preferenceService.long("camera.${device.name}.exposureDelay", view.exposureDelay)
            preferenceService.bool("camera.${device.name}.isSubFrame", view.isSubFrame)
            preferenceService.int("camera.${device.name}.frameX", view.frameX)
            preferenceService.int("camera.${device.name}.frameY", view.frameY)
            preferenceService.int("camera.${device.name}.frameWidth", view.frameWidth)
            preferenceService.int("camera.${device.name}.frameHeight", view.frameHeight)
            preferenceService.enum("camera.${device.name}.frameType", view.frameType)
            preferenceService.string("camera.${device.name}.frameFormat", view.frameFormat)
            preferenceService.int("camera.${device.name}.binX", view.binX)
            preferenceService.int("camera.${device.name}.binY", view.binY)
            preferenceService.int("camera.${device.name}.gain", view.gain)
            preferenceService.int("camera.${device.name}.offset", view.offset)
        }

        preferenceService.double("camera.screen.x", max(0.0, view.x))
        preferenceService.double("camera.screen.y", max(0.0, view.y))
    }

    fun loadPreferences(device: Camera? = value) {
        if (device != null) {
            updateMaxBin()
            updateExposureMinMax()
            updateFrame()
            updateGainMinMax()
            updateOffsetMinMax()

            view.temperatureSetpoint = preferenceService.double("camera.${device.name}.temperatureSetpoint") ?: 0.0
            val exposureUnit = preferenceService.enum("camera.${device.name}.exposureUnit") ?: TimeUnit.MICROSECONDS
            val exposureTimeInMicros = preferenceService.long("camera.${device.name}.exposure") ?: exposureMin
            view.exposureCount = preferenceService.int("camera.${device.name}.exposureCount") ?: 1
            view.exposureMode = preferenceService.enum<ExposureMode>("camera.${device.name}.exposureMode") ?: ExposureMode.SINGLE
            view.exposureDelay = preferenceService.long("camera.${device.name}.exposureDelay") ?: 100L
            view.isSubFrame = preferenceService.bool("camera.${device.name}.isSubFrame")
            val frameX = preferenceService.int("camera.${device.name}.frameX") ?: minX
            val frameY = preferenceService.int("camera.${device.name}.frameY") ?: minY
            val frameWidth = preferenceService.int("camera.${device.name}.frameWidth") ?: maxWidth
            val frameHeight = preferenceService.int("camera.${device.name}.frameHeight") ?: maxHeight
            view.updateFrame(frameX, frameY, frameWidth, frameHeight)
            view.frameType = preferenceService.enum<FrameType>("camera.${device.name}.frameType") ?: FrameType.LIGHT
            (preferenceService.string("camera.${device.name}.frameFormat") ?: frameFormats.firstOrNull())?.let { view.frameFormat = it }
            val binX = preferenceService.int("camera.${device.name}.binX") ?: 1
            val binY = preferenceService.int("camera.${device.name}.binY") ?: 1
            val gain = preferenceService.int("camera.${device.name}.gain") ?: 0
            val offset = preferenceService.int("camera.${device.name}.offset") ?: 0
            view.updateBin(binX, binY)
            view.updateGainAndOffset(gain, offset)

            updateExposureUnit(TimeUnit.MICROSECONDS, exposureUnit, exposureTimeInMicros)

            view.isAutoSaveAllExposures = preferenceService.bool("camera.${device.name}.autoSaveAllExposures")
            view.isAutoSubFolder = preferenceService.bool("camera.${device.name}.autoSubFolder")
            view.autoSubFolderMode = preferenceService.enum<AutoSubFolderMode>("camera.${device.name}.newSubFolderAt") ?: AutoSubFolderMode.NOON
            view.imageSavePath =
                preferenceService.string("camera.${device.name}.imageSavePath") ?: Paths.get("$appDirectory", "captures", device.name).toString()
        }

        if (device == null || !capturingProperty.get()) {
            preferenceService.double("camera.screen.x")?.let { view.x = it }
            preferenceService.double("camera.screen.y")?.let { view.y = it }
        }
    }

    override fun close() {
        super.close()

        savePreferences()

        imageViews.forEach(View::close)
        imageViews.clear()

        unregisterListener(this)

        eventBus.unregister(this)
    }
}
