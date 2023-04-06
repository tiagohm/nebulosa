package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.*
import nebulosa.desktop.logic.DevicePropertyListener
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.guider.DitherMode
import nebulosa.desktop.view.guider.GuideAlgorithmType
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.guiding.*
import nebulosa.guiding.internal.*
import nebulosa.imaging.Image
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraFrameCaptured
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.PierSide
import nebulosa.math.Angle
import nom.tam.fits.Fits
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.hypot

@Component
class GuiderManager(
    @Autowired internal val view: GuiderView,
    @Autowired internal val equipmentManager: EquipmentManager,
) : GuideDevice, GuiderListener {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var guiderExecutorService: ExecutorService
    @Autowired private lateinit var systemExecutorService: ExecutorService
    @Autowired private lateinit var javaFXExecutorService: ExecutorService
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView

    private val guideCameraPropertyListener = GuideCameraPropertyListener()
    private val guideMountPropertyListener = GuideMountPropertyListener()
    private val guideOutputPropertyListener = GuideOutputPropertyListener()
    private val imageQueue = LinkedBlockingQueue<Image>()

    private lateinit var guider: Guider
    private lateinit var guiderIndicator: GuiderIndicator
    @Volatile private var imageView: ImageView? = null

    private val xGuideAlgorithms = hashMapOf(
        GuideAlgorithmType.HYSTERESIS to HysteresisGuideAlgorithm(GuideAxis.RA_X),
        GuideAlgorithmType.LOW_PASS to LowPassGuideAlgorithm(GuideAxis.RA_X),
        GuideAlgorithmType.RESIST_SWITCH to ResistSwitchGuideAlgorithm(GuideAxis.RA_X),
    )

    private val yGuideAlgorithms = hashMapOf(
        GuideAlgorithmType.HYSTERESIS to HysteresisGuideAlgorithm(GuideAxis.DEC_Y),
        GuideAlgorithmType.LOW_PASS to LowPassGuideAlgorithm(GuideAxis.DEC_Y),
        GuideAlgorithmType.RESIST_SWITCH to ResistSwitchGuideAlgorithm(GuideAxis.DEC_Y),
    )

    private val randomDither = RandomDither()
    private val spiralDither = SpiralDither()

    val loopingProperty = SimpleBooleanProperty()
    val guidingProperty = SimpleBooleanProperty()

    val cameras
        get() = equipmentManager.attachedCameras

    val mounts
        get() = equipmentManager.attachedMounts

    val guideOutputs
        get() = equipmentManager.attachedGuideOutputs

    val selectedGuideCamera
        get() = equipmentManager.selectedGuideCamera

    val selectedGuideMount
        get() = equipmentManager.selectedGuideMount

    val selectedGuideOutput
        get() = equipmentManager.selectedGuideOutput

    val camera: Camera?
        get() = selectedGuideCamera.value

    val mount: Mount?
        get() = selectedGuideMount.value

    val guideOutput: GuideOutput?
        get() = selectedGuideOutput.value

    fun initialize() {
        selectedGuideCamera.registerListener(guideCameraPropertyListener)
        selectedGuideMount.registerListener(guideMountPropertyListener)
        selectedGuideOutput.registerListener(guideOutputPropertyListener)

        with(MultiStarGuider(this, guiderExecutorService)) {
            registerListener(this@GuiderManager)
            guider = this
            guiderIndicator = GuiderIndicator(this)
        }
    }

    fun connectGuideCamera() {
        val camera = camera ?: return
        if (camera.connected) camera.disconnect()
        else camera.connect()
    }

    fun connectGuideMount() {
        val mount = mount ?: return
        if (mount.connected) mount.disconnect()
        else mount.connect()
    }

    fun connectGuideOutput() {
        val guideOutput = guideOutput ?: return
        if (guideOutput.connected) guideOutput.disconnect()
        else guideOutput.connect()
    }

    fun openINDIPanelControlForGuideCamera() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = camera
    }

    fun openINDIPanelControlForGuideMount() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = mount
    }

    fun openINDIPanelControlForGuideOutput() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = guideOutput
    }

    fun startLooping() {
        loopingProperty.set(true)
        camera?.enableBlob()
        guider.startLooping()
    }

    fun stopLooping() {
        loopingProperty.set(false)
        camera?.disableBlob()
        guider.stopLooping()
    }

    fun startGuiding(forceCalibration: Boolean = false) {
        guidingProperty.set(true)

        xGuideAlgorithms.values.forEach(GuideAlgorithm::reset)
        yGuideAlgorithms.values.forEach(GuideAlgorithm::reset)

        if (forceCalibration) {
            LOG.info("starting guiding with force calibration")
            guider.clearCalibration()
        } else {
            try {
                val json = preferences.json<Map<String, String>>("guider.${camera?.name}.${mount?.name}.calibration")
                val calibration = json?.let(::Calibration)

                if (calibration != null && calibration != Calibration.EMPTY) {
                    LOG.info("calibration restored. calibration={}", calibration)
                    guider.loadCalibration(calibration)
                } else {
                    LOG.info("unable to restore calibration")
                }
            } catch (e: Throwable) {
                LOG.error("calibration restoration error", e)
            }
        }

        guider.startGuiding()
    }

    fun stopGuiding() {
        guider.stopGuiding()
    }

    fun selectGuideStar(x: Double, y: Double) {
        if (guider.selectGuideStar(x, y)) {
            GlobalScope.launch(Dispatchers.Main) {
                guiderIndicator.redraw()
                view.updateStarProfile(guider)
            }
        }
    }

    fun deselectGuideStar() {
        guider.deselectGuideStar()
    }

    // Camera.

    override val cameraBinning
        get() = camera?.binX ?: 1

    override val cameraPixelScale
        get() = camera?.pixelSizeX ?: 0.0

    override val cameraExposureTime
        get() = view.exposureTime

    override val cameraExposureDelay
        get() = view.exposureDelay

    override fun capture(duration: Long): Image {
        camera?.startCapture(duration * 1000L)
        return imageQueue.take()
    }

    // Mount.

    override val mountIsSlewing
        get() = mount?.slewing ?: false

    override val mountDeclination
        get() = mount?.declination ?: Angle.NaN

    override val mountRightAscension
        get() = mount?.rightAscension ?: Angle.NaN

    override val mountRightAscensionGuideRate
        get() = 0.5 // TODO: Implement Guide Rate property on mount device.

    override val mountDeclinationGuideRate
        get() = 0.5

    override val mountPierSideAtEast
        get() = mount?.pierSide == PierSide.EAST

    // Rotator.

    override val rotatorAngle
        get() = Angle.ZERO // TODO: Pass the rotator angle when implement it.

    // Guiding.

    override val dither
        get() = if (view.ditherMode == DitherMode.RANDOM) randomDither else spiralDither

    override val ditherAmount
        get() = view.ditherAmount

    override val ditherRAOnly
        get() = view.ditherRAOnly

    override var calibrationFlipRequiresDecFlip = false

    override val calibrationStep
        get() = view.calibrationStep

    override val declinationGuideMode
        get() = view.guideModeDEC

    override val guidingEnabled
        get() = true

    override val maxDECDuration
        get() = view.maxDurationDEC

    override val maxRADuration
        get() = view.maxDurationRA

    override val calibrationDistance
        get() = view.calibrationStep

    override val xGuideAlgorithm
        get() = xGuideAlgorithms[view.algorithmRA]!!.updateParameters()

    override val yGuideAlgorithm
        get() = yGuideAlgorithms[view.algorithmDEC]!!.updateParameters()

    override val useDECCompensation
        get() = view.useDECCompensation

    override val assumeDECOrthogonalToRA
        get() = view.assumeDECOrthogonalToRA

    override val searchRegion
        get() = view.searchRegion

    override val noiseReductionMethod
        get() = view.noiseReductionMethod

    override fun guideNorth(duration: Int): Boolean {
        val guideOutput = guideOutput ?: return false
        LOG.info("guiding north. output={}, duration={} ms", guideOutput.name, duration)
        return guideTo(guideOutput::guideNorth, duration)
    }

    override fun guideSouth(duration: Int): Boolean {
        val guideOutput = guideOutput ?: return false
        LOG.info("guiding south. output={}, duration={} ms", guideOutput.name, duration)
        return guideTo(guideOutput::guideSouth, duration)
    }

    override fun guideWest(duration: Int): Boolean {
        val guideOutput = guideOutput ?: return false
        LOG.info("guiding west. output={}, duration={} ms", guideOutput.name, duration)
        return guideTo(guideOutput::guideWest, duration)
    }

    override fun guideEast(duration: Int): Boolean {
        val guideOutput = guideOutput ?: return false
        LOG.info("guiding east. output={}, duration={} ms", guideOutput.name, duration)
        return guideTo(guideOutput::guideEast, duration)
    }

    private inline fun guideTo(crossinline callback: (Int) -> Unit, duration: Int): Boolean {
        guiderExecutorService.submit { callback(duration) }
        return true
    }

    override fun onLockPositionChanged(position: GuidePoint) {
        runBlocking { view.updateStatus("lock position changed. x=%.1f, y=%.1f".format(position.x, position.y)) }
    }

    override fun onStarSelected(star: StarPoint) {
        runBlocking {
            view.updateStatus(
                "star selected. x=%.1f, y=%.1f, mass=%.1f, hfd=%.1f, snr=%.1f, peak=%.1f".format(
                    star.x, star.y, star.mass, star.hfd, star.snr, star.peak,
                )
            )
        }
    }

    override fun onGuidingDithered(dx: Double, dy: Double) {
        LOG.info("guiding dither. dx={}, dy={}", dx, dy)
    }

    override fun onCalibrationFailed() {
        runBlocking { view.updateStatus("calibration failed") }
    }

    override fun onGuidingStopped() {
        guidingProperty.set(false)
        runBlocking { view.updateStatus("guiding stopped") }
    }

    override fun onLockShiftLimitReached() {
        runBlocking { view.updateStatus("lock shift limit reached") }
    }

    override fun onLooping(image: Image, number: Int, star: StarPoint?) {
        runBlocking {
            view.updateStatus("looping. number=$number")
            imageView?.also { it.open(image, null) }
            guiderIndicator.redraw()
            view.updateStarProfile(guider, image)
        }
    }

    override fun onStarLost() {
        runBlocking { view.updateStatus("star lost") }
    }

    override fun onLockPositionLost() {
        runBlocking { view.updateStatus("lock position lost") }
    }

    override fun onStartCalibration() {
        runBlocking { view.updateStatus("calibration started") }
    }

    override fun onCalibrationStep(
        calibrationState: CalibrationState,
        direction: GuideDirection, stepNumber: Int,
        dx: Double, dy: Double,
        posX: Double, posY: Double, distance: Double,
    ) {
        LOG.info(
            "calibration step. state={}, direction={}, step={}, dx={}, dy={}, x={}, y={}, distance={}",
            calibrationState, direction, stepNumber, dx, dy, posX, posY, distance,
        )
    }

    override fun onCalibrationCompleted(calibration: Calibration) {
        runBlocking { view.updateStatus("calibration completed") }
        preferences.json("guider.${camera?.name}.${mount?.name}.calibration", calibration.toMap())
    }

    override fun onGuideStep(stats: GuideStats) {
        LOG.info("guiding step. RMS RA={}, RMS DEC={} dx={}, dy={}", stats.rmsRA, stats.rmsDEC, stats.dx, stats.dy)

        runBlocking {
            view.updateGraph(guider.stats, maxRADuration.toDouble(), maxDECDuration.toDouble())
            val rmsTotal = hypot(stats.rmsRA, stats.rmsDEC)
            view.updateGraphInfo(stats.rmsRA, stats.rmsDEC, rmsTotal, cameraPixelScale)
        }
    }

    private fun GuideAlgorithm.updateParameters(): GuideAlgorithm {
        val axisRA = axis == GuideAxis.RA_X

        minMove = if (axisRA) view.minimumMoveRA else view.minimumMoveDEC

        when (this) {
            is HysteresisGuideAlgorithm -> {
                hysteresis = if (axisRA) view.hysteresisRA else view.hysteresisDEC
                aggression = if (axisRA) view.aggressivenessRA else view.aggressivenessDEC
            }
            is LowPassGuideAlgorithm -> {
                slopeWeight = if (axisRA) view.slopeWeightRA else view.slopeWeightDEC
            }
            is ResistSwitchGuideAlgorithm -> {
                aggression = if (axisRA) view.aggressivenessRA else view.aggressivenessDEC
                fastSwitchForLargeDeflections = if (axisRA) view.fastSwitchForLargeDeflectionsRA else view.fastSwitchForLargeDeflectionsDEC
            }
        }

        return this
    }

    fun loadPreferences(
        camera: Camera? = this.camera,
        mount: Mount? = this.mount,
        guideOutput: GuideOutput? = this.guideOutput,
    ) {
        if (camera != null && mount != null && guideOutput != null) {

        }
    }

    fun savePreferences(
        camera: Camera? = this.camera,
        mount: Mount? = this.mount,
        guideOutput: GuideOutput? = this.guideOutput,
    ) {
        if (!view.showing) return
    }

    private inner class GuideCameraPropertyListener : DevicePropertyListener<Camera> {

        override fun onChanged(prev: Camera?, device: Camera) {
            if (prev !== device) savePreferences(prev)

            loadPreferences(device)
        }

        override fun onReset() {}

        override fun onDeviceEvent(event: DeviceEvent<*>, device: Camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            val fits = Fits(event.fits)
                            val image = Image.open(fits)
                            imageQueue.offer(image)

                            withContext(Dispatchers.Main) {
                                if (imageView == null) {
                                    imageView = imageViewOpener.open(image, null, device)
                                    imageView!!.registerMouseListener(view)
                                    imageView!!.addFirst(guiderIndicator)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class GuideMountPropertyListener : DevicePropertyListener<Mount> {

        override fun onChanged(prev: Mount?, device: Mount) {
            if (prev !== device) savePreferences(mount = prev)

            loadPreferences(mount = device)
        }

        override fun onReset() {}

        override fun onDeviceEvent(event: DeviceEvent<*>, device: Mount) {}
    }

    private inner class GuideOutputPropertyListener : DevicePropertyListener<GuideOutput> {

        override fun onChanged(prev: GuideOutput?, device: GuideOutput) {
            if (prev !== device) savePreferences(guideOutput = prev)

            loadPreferences(guideOutput = device)
        }

        override fun onReset() {}

        override fun onDeviceEvent(event: DeviceEvent<*>, device: GuideOutput) {}
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuiderManager::class.java)
    }
}
