package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.camera.CameraProperty
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
) : CameraProperty by equipmentManager.selectedGuider, GuideCamera, GuideMount {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView

    private val guider = MultiStarGuider()
    private val guideStarBox = GuideStarBox(guider)
    private val imageQueue = LinkedBlockingQueue<Image>()
    private var imageView: ImageView? = null

    val guidingProperty = SimpleBooleanProperty()

    val cameras
        get() = equipmentManager.attachedCameras

    val mounts
        get() = equipmentManager.attachedMounts

    val mount: Mount?
        get() = equipmentManager.selectedMount.value

    fun initialize() {
        registerListener(this)

        guider.attachGuideCamera(this)
        guider.attachGuideMount(this)

        // eventBus.register(this)
    }

    fun openINDIPanelControl() {
        indiPanelControlView.show(bringToFront = true)
        indiPanelControlView.device = value
    }

    @Synchronized
    fun start() {
        guidingProperty.set(true)
        value?.enableBlob()
        guider.startLooping()
    }

    @Synchronized
    fun stop() {
        guidingProperty.set(false)
        value?.disableBlob()
        guider.stopLooping()
    }

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

    // Camera.

    override val image: Image
        get() = imageQueue.take()

    override val pixelScale
        get() = value?.pixelSizeX ?: 0.0

    override val exposure
        get() = 5000L

    override fun capture(duration: Long) {
        value?.startCapture(duration * 1000L)
    }

    // Mount.

    override var busy = false

    override var calibrationFlipRequiresDecFlip = false

    override var raParity = GuideParity.UNCHANGED

    override var decParity = GuideParity.UNCHANGED

    override val declination: Angle
        get() = mount?.declination ?: Angle.NaN

    override var guidingEnabled = true

    override var declinationGuideMode = DeclinationGuideMode.AUTO

    override var maxDeclinationDuration = 2000

    override var maxRightAscensionDuration = 2000

    override var xGuideAlgorithm = HysteresisGuideAlgorithm(GuideAxis.RA_X)

    override var yGuideAlgorithm = HysteresisGuideAlgorithm(GuideAxis.DEC_Y)

    override fun moveTo(direction: GuideDirection, duration: Int): Boolean {
        val mount = mount ?: return false

        if (!mount.canPulseGuide) return false

        when (direction) {
            GuideDirection.UP_NORTH -> mount.guideNorth(duration)
            GuideDirection.DOWN_SOUTH -> mount.guideSouth(duration)
            GuideDirection.LEFT_WEST -> mount.guideWest(duration)
            GuideDirection.RIGHT_EAST -> mount.guideEast(duration)
            else -> return false
        }

        return true
    }

    override fun beginCalibration(currentLocation: Point): Boolean {
        println("beginCalibration. $currentLocation")
        return false
    }

    override fun updateCalibrationState(currentLocation: Point): Boolean {
        println("updateCalibrationState. $currentLocation")
        return false
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

    fun selectGuideStar(x: Double, y: Double) {
        guider.selectGuideStar(x, y)
    }

    fun deselectGuideStar() {
        guider.deselectGuideStar()
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuiderManager::class.java)
    }
}
