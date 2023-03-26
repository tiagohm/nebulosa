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
) : GuideCamera, GuideMount, GuiderListener {

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

        guider.attachGuideCamera(this)
        guider.attachGuideMount(this)
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

    override val connected
        get() = camera?.connected == true && mount?.connected == true

    // Camera.

    override val image: Image
        get() = imageQueue.take()

    override val pixelScale
        get() = camera?.pixelSizeX ?: 0.0

    override val exposure
        get() = 5000L

    override fun capture(duration: Long) {
        camera?.startCapture(duration * 1000L)
    }

    // Mount.

    override var busy = false

    override var calibrationFlipRequiresDecFlip = false

    override var calibrationDuration = GuideMount.DEFAULT_CALIBRATION_DURATION

    override var raParity = GuideParity.UNCHANGED

    override var decParity = GuideParity.UNCHANGED

    override val declination
        get() = mount?.declination ?: Angle.NaN

    override var guidingEnabled = true

    override var declinationGuideMode = DeclinationGuideMode.AUTO

    override var maxDeclinationDuration = 2000

    override var maxRightAscensionDuration = 2000

    override val calibrationDistance
        get() = 25 // px

    override val rightAscension
        get() = mount?.rightAscension ?: Angle.NaN

    override val rightAscensionGuideRate
        get() = 0.5

    override val declinationGuideRate
        get() = 0.5

    override var xGuideAlgorithm = HysteresisGuideAlgorithm(GuideAxis.RA_X)

    override var yGuideAlgorithm = HysteresisGuideAlgorithm(GuideAxis.DEC_Y)

    override fun guideTo(direction: GuideDirection, duration: Int): Boolean {
        val mount = mount ?: return false

        if (!mount.canPulseGuide) return false

        LOG.info("guiding. direction={}, duration={}", direction, duration)

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

    override fun notifyGuidingStarted() {
        println("notifyGuidingStarted")
    }

    override fun notifyGuidingStopped() {
        println("notifyGuidingStopped")
    }

    override fun notifyGuidingPaused() {
        println("notifyGuidingPaused")
    }

    override fun notifyGuidingResumed() {
        println("notifyGuidingResumed")
    }

    override fun notifyGuidingDithered(dx: Double, dy: Double, mountCoords: Boolean) {
        println("notifyGuidingDithered. $dx $dy $mountCoords")
    }

    override fun notifyGuidingDitherSettleDone(success: Boolean) {
        println("notifyGuidingDitherSettleDone")
    }

    override fun notifyDirectMove(distance: Point) {
        println("notifyDirectMove")
    }

    override fun onLockPositionChanged(guider: MultiStarGuider, position: Point) {
    }

    override fun onStarSelected(guider: MultiStarGuider, star: Star) {
    }

    override fun onGuidingDithered(guider: MultiStarGuider, dx: Double, dy: Double, mountCoordinate: Boolean) {
    }

    override fun onCalibrationFailed() {
    }

    override fun onGuidingStopped() {
    }

    override fun onLockShiftLimitReached() {
    }

    override fun onLooping(frameNumber: Int, start: Star?) {
        LOG.info("looping. number={}", frameNumber)
    }

    override fun onStarLost() {
    }

    override fun onLockPositionLost() {
    }

    override fun onStartCalibration() {
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
