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

    override var calibrated = false

    override var raParity = GuideParity.UNCHANGED

    override var decParity = GuideParity.UNCHANGED

    override val declination: Angle
        get() = mount?.declination ?: Angle.NaN

    override var guidingEnabled = true

    override var declinationGuideMode = DeclinationGuideMode.AUTO

    override var xGuideAlgorithm = HysteresisGuideAlgorithm()

    override var yGuideAlgorithm = HysteresisGuideAlgorithm()

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

    override fun transformMountCoordinatesToCameraCoordinates(mount: Point, camera: Point): Boolean {
        val distance = mount.distance
        val mountTheta = mount.angle
        // val xAngle = mountTheta + calibration.xAngle
        // camera.set(cos(xAngle) * distance, sin(xAngle) * distance)
        return true
    }

    override fun moveOffset(offset: GuiderOffset, moveOptions: List<MountMoveOption>): Boolean {
        if (MountMoveOption.ALGORITHM_DEDUCE in moveOptions) {
            val xDistance = xGuideAlgorithm.deduce()
            val yDistance = yGuideAlgorithm.deduce()

            if (xDistance == 0.0 && yDistance == 0.0) return true

            offset.mount.set(xDistance, yDistance)
        } else {
            if (!offset.mount.valid) {
                if (!transformCameraCoordinatesToMountCoordinates(offset.camera, offset.mount)) {
                    println("Unable to transform camera coordinates")
                    return false
                }
            }

            var xDistance = offset.mount.x
            var yDistance = offset.mount.y

            // Let BLC track the raw offsets in Dec
            // TODO: if (m_backlashComp)
            //    m_backlashComp->TrackBLCResults(moveOptions, yDistance)

            if (MountMoveOption.ALGORITHM_RESULT in moveOptions) {
                xDistance = xGuideAlgorithm.compute(xDistance)
                yDistance = yGuideAlgorithm.compute(yDistance)
            }

            // Figure out the guide directions based on the (possibly) updated distances
            val xDirection = if (xDistance > 0.0) GuideDirection.LEFT_WEST else GuideDirection.RIGHT_EAST
            val yDirection = if (yDistance > 0.0) GuideDirection.DOWN_SOUTH else GuideDirection.UP_NORTH

            println("moving $xDistance $yDistance $xDirection $yDirection")

            // TODO: Setar a calibração val requestedXAmount = abs(xDistance / xRate).round()
        }

        return true
    }

    override fun transformCameraCoordinatesToMountCoordinates(camera: Point, mount: Point): Boolean {
        return true
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
