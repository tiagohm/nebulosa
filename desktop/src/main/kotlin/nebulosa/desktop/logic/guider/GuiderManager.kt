package nebulosa.desktop.logic.guider

import javafx.beans.property.SimpleBooleanProperty
import nebulosa.desktop.logic.Preferences
import nebulosa.desktop.logic.camera.CameraProperty
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.equipment.EquipmentManager
import nebulosa.desktop.view.guider.GuiderView
import nebulosa.desktop.view.image.ImageView
import nebulosa.desktop.view.indi.INDIPanelControlView
import nebulosa.guiding.internal.GuideCamera
import nebulosa.guiding.internal.MultiStarGuider
import nebulosa.imaging.Image
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraFrameCaptured
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
) : CameraProperty by equipmentManager.selectedGuider, GuideCamera {

    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var eventBus: EventBus
    @Autowired private lateinit var imageViewOpener: ImageView.Opener
    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService
    @Autowired private lateinit var indiPanelControlView: INDIPanelControlView

    private val guider = MultiStarGuider()

    private val imageQueue = LinkedBlockingQueue<Image>()

    val guidingProperty = SimpleBooleanProperty()

    val cameras
        get() = equipmentManager.attachedCameras

    fun initialize() {
        registerListener(this)

        guider.attach(this)

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

                javaFXExecutorService.submit { imageViewOpener.open(image, null, device) }
            }
        }
    }

    override val image: Image
        get() = imageQueue.take()

    override val pixelScale
        get() = value?.pixelSizeX ?: 0.0

    override val exposure
        get() = 1000L

    override var autoExposure = false

    override fun capture(duration: Long) {
        value?.startCapture(duration * 1000L)
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(GuiderManager::class.java)
    }
}
