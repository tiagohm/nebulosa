package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.DevicePropertyListener
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.guiding.internal.*
import nebulosa.imaging.Image
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraFrameCaptured
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.PierSide
import nebulosa.math.Angle
import nom.tam.fits.Fits
import org.greenrobot.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue

@Component
class GuiderManager(
    @Autowired internal val view: GuiderView,
    @Autowired internal val equipmentManager: EquipmentManager,
) : GuideDevice, GuiderListener {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView

    private val cameraPropertyListener = CameraPropertyListener()
    private val guider = MultiStarGuider()
    private val guideStarBox = GuideStarBox(guider)
    private val imageQueue = LinkedBlockingQueue<Image>()
    private var imageView: ImageView? = null

    val guidingProperty = SimpleBooleanProperty()

    val cameras
        get() = equipmentManager.attachedCameras

    val mounts
        get() = equipmentManager.attachedMounts

    val selectedGuideCamera
        get() = equipmentManager.selectedGuideCamera

    val selectedGuideMount
        get() = equipmentManager.selectedGuideMount

    val camera: Camera?
        get() = selectedGuideCamera.value

    val mount: Mount?
        get() = selectedGuideMount.value

    fun initialize() {
        selectedGuideCamera.registerListener(cameraPropertyListener)
        // selectedGuiderMount.registerListener(this)

        guider.attachGuideDevice(this)
        guider.registerListener(this)

        // eventBus.register(this)
    }

    fun connectGuideCamera() {
        camera?.connect()
    }

    fun connectGuideMount() {
        mount?.connect()
    }

    fun openINDIPanelControlForGuideCamera() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = camera
    }

    fun openINDIPanelControlForGuideMount() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = mount
    }

    @Synchronized
    fun startLooping() {
        guidingProperty.set(true)
        camera?.enableBlob()
        guider.startLooping()
    }

    @Synchronized
    fun stopLooping() {
        guidingProperty.set(false)
        camera?.disableBlob()
        guider.stopLooping()
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
        get() = mount?.connected == true

    override var mountIsBusy = false

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

    override fun guideTo(direction: GuideDirection, duration: Int): Boolean {
        val mount = mount ?: return false

        if (!mount.canPulseGuide) return false

        LOG.info("guiding. direction={}, duration={}", direction, duration)

        if (duration <= 0) return true

        when (direction) {
            GuideDirection.UP_NORTH -> mount.guideNorth(duration)
            GuideDirection.DOWN_SOUTH -> mount.guideSouth(duration)
            GuideDirection.LEFT_WEST -> mount.guideWest(duration)
            GuideDirection.RIGHT_EAST -> mount.guideEast(duration)
            else -> return false
        }

        Thread.sleep(duration.toLong())

        return true
    }

    override fun onLockPositionChanged(position: Point) {
        LOG.info("lock position changed. x={}, y={}", position.x, position.y)
    }

    override fun onStarSelected(star: Star) {
        LOG.info("star selected. x={}, y={}, mass={}, hfd={}, snr={}, peak={}", star.x, star.y, star.mass, star.hfd, star.snr, star.peak)
    }

    override fun onGuidingDithered(dx: Double, dy: Double, mountCoordinate: Boolean) {
        LOG.info("guiding dither. dx={}, dy={}, mountCoordinate={}", dx, dy, mountCoordinate)
    }

    override fun onCalibrationFailed() {
        LOG.info("calibration failed")
    }

    override fun onGuidingStopped() {
        LOG.info("guiding stopped")
    }

    override fun onLockShiftLimitReached() {
        LOG.info("lock shift limit reached")
    }

    override fun onLooping(frameNumber: Int, start: Star?) {
        LOG.info("looping. number={}", frameNumber)
    }

    override fun onStarLost() {
        LOG.info("star lost")
    }

    override fun onLockPositionLost() {
        LOG.info("lock position lost")
    }

    override fun onStartCalibration() {
        LOG.info("start calibration")
    }

    override fun onCalibrationStep(
        calibrationState: CalibrationState,
        direction: GuideDirection, stepNumber: Int,
        dx: Double, dy: Double,
        posX: Double, posY: Double, distance: Double
    ) {
        LOG.info(
            "calibration step. state={}, direction={}, step={}, dx={}, dy={}, x={}, y={}, distance={}",
            calibrationState, direction, stepNumber, dx, dy, posX, posY, distance,
        )
    }

    override fun onCalibrationCompleted() {
    }

    fun selectGuideStar(x: Double, y: Double) {
        if (guider.selectGuideStar(x, y)) {
            guider.startGuiding()
        }
    }

    fun deselectGuideStar() {
        guider.deselectGuideStar()
    }

    private inner class CameraPropertyListener : DevicePropertyListener<Camera> {

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
                            imageView!!.imageViewer.addFirst(guideStarBox)
                        } else {
                            imageView!!.open(image, null)
                        }
                    }
                }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuiderManager::class.java)
    }
}
