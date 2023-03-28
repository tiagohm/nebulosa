package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.DevicePropertyListener
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.guiding.*
import nebulosa.guiding.internal.DeclinationGuideMode
import nebulosa.guiding.internal.GuideDevice
import nebulosa.guiding.internal.HysteresisGuideAlgorithm
import nebulosa.guiding.internal.MultiStarGuider
import nebulosa.imaging.Image
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraFrameCaptured
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.PierSide
import nebulosa.math.Angle
import nom.tam.fits.Fits
import org.greenrobot.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue

@Component
class GuiderManager(
    @Autowired internal val view: GuiderView,
    @Autowired internal val equipmentManager: EquipmentManager,
) : GuideDevice, GuiderListener {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var guiderExecutorService: ExecutorService
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView

    private val guideCameraPropertyListener = GuideCameraPropertyListener()
    private val guideOutputPropertyListener = GuideOutputPropertyListener()
    private lateinit var guider: Guider
    private lateinit var guiderIndicator: GuiderIndicator
    private val imageQueue = LinkedBlockingQueue<Image>()
    private val pulseGuidingLatch = CountUpDownLatch()
    @Volatile private var imageView: ImageView? = null

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
        // selectedGuiderMount.registerListener(this)
        selectedGuideOutput.registerListener(guideOutputPropertyListener)

        with(MultiStarGuider(this, guiderExecutorService)) {
            registerListener(this@GuiderManager)
            guider = this
            guiderIndicator = GuiderIndicator(this)
        }

        // eventBus.register(this)
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
            imageView?.redraw()
        }
    }

    fun deselectGuideStar() {
        guider.deselectGuideStar()
    }

    // Camera.

    override val cameraIsConnected
        get() = camera?.connected == true

    override val cameraBinning
        get() = camera?.binX ?: 1

    override val cameraImage: Image
        get() = imageQueue.take()

    override val cameraPixelScale
        get() = camera?.pixelSizeX ?: 0.0

    override val cameraExposure
        get() = 5000L

    override fun capture(duration: Long) {
        camera?.startCapture(duration * 1000L)
    }

    // Mount.

    override val mountIsConnected
        get() = mount?.connected ?: false

    override val mountDeclination
        get() = mount?.declination ?: Angle.NaN

    override val mountRightAscension
        get() = mount?.rightAscension ?: Angle.NaN

    override val mountRightAscensionGuideRate
        get() = 0.5

    override val mountDeclinationGuideRate
        get() = 0.5

    override val mountPierSideAtEast
        get() = mount?.pierSide == PierSide.EAST

    override fun awaitIfMountIsBusy() {
        pulseGuidingLatch.await()
    }

    // Rotator.

    override val rotatorAngle
        get() = Angle.ZERO

    // Guiding.

    override var calibrationFlipRequiresDecFlip = false

    override var calibrationDuration = GuideDevice.DEFAULT_CALIBRATION_DURATION

    override var rightAscensionParity = GuideParity.UNCHANGED

    override var declinationParity = GuideParity.UNCHANGED

    override var declinationGuideMode = DeclinationGuideMode.AUTO

    override var guidingEnabled = true

    override var maxDeclinationDuration = 2000

    override var maxRightAscensionDuration = 2000

    override val calibrationDistance
        get() = 25 // px

    override var xGuideAlgorithm = HysteresisGuideAlgorithm(GuideAxis.RA_X)

    override var yGuideAlgorithm = HysteresisGuideAlgorithm(GuideAxis.DEC_Y)

    override var declinationCompensationEnabled = true

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
        pulseGuidingLatch.countUp()
        guiderExecutorService.submit { callback(duration) }
        return true
    }

    override fun onLockPositionChanged(position: GuidePoint) {
        view.updateStatus("lock position changed. x=%.1f, y=%.1f".format(position.x, position.y))
    }

    override fun onStarSelected(star: StarPoint) {
        view.updateStatus(
            "star selected. x=%.1f, y=%.1f, mass=%.1f, hfd=%.1f, snr=%.1f, peak=%.1f".format(
                star.x, star.y, star.mass, star.hfd, star.snr, star.peak,
            )
        )
    }

    override fun onGuidingDithered(dx: Double, dy: Double, mountCoordinate: Boolean) {
        LOG.info("guiding dither. dx={}, dy={}, mountCoordinate={}", dx, dy, mountCoordinate)
    }

    override fun onCalibrationFailed() {
        view.updateStatus("calibration failed")
    }

    override fun onGuidingStopped() {
        guidingProperty.set(false)
        view.updateStatus("guiding stopped")
    }

    override fun onLockShiftLimitReached() {
        view.updateStatus("lock shift limit reached")
    }

    override fun onLooping(image: Image, number: Int, star: StarPoint?) {
        view.updateStatus("looping. number=$number")
        imageView?.also { javaFXExecutorService.submit { it.open(image, null) } }
    }

    override fun onStarLost() {
        view.updateStatus("star lost")
    }

    override fun onLockPositionLost() {
        view.updateStatus("lock position lost")
    }

    override fun onStartCalibration() {
        view.updateStatus("calibration started")
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
        view.updateStatus("calibration completed")
        preferences.json("guider.${camera?.name}.${mount?.name}.calibration", calibration.toMap())
    }

    private inner class GuideCameraPropertyListener : DevicePropertyListener<Camera> {

        override fun onChanged(prev: Camera?, device: Camera) {}

        override fun onReset() {}

        override fun onDeviceEvent(event: DeviceEvent<*>, device: Camera) {
            when (event) {
                is CameraFrameCaptured -> {
                    val fits = Fits(event.fits)
                    val image = Image.open(fits)
                    imageQueue.offer(image)

                    javaFXExecutorService.submit {
                        if (imageView == null) {
                            imageView = imageViewOpener.open(image, null, device)
                            imageView!!.imageViewer.registerMouseListener(view)
                            imageView!!.imageViewer.addFirst(guiderIndicator)
                        }
                    }
                }
            }
        }
    }

    private inner class GuideOutputPropertyListener : DevicePropertyListener<GuideOutput> {

        override fun onChanged(prev: GuideOutput?, device: GuideOutput) {}

        override fun onReset() {}

        override fun onDeviceEvent(event: DeviceEvent<*>, device: GuideOutput) {
            when (event) {
                is GuideOutputPulsingChanged -> {
                    if (!device.pulseGuiding) {
                        pulseGuidingLatch.countDown()
                    }
                }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuiderManager::class.java)
    }
}
