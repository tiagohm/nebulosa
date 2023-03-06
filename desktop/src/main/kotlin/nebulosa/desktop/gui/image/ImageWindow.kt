package nebulosa.desktop.gui.image

import com.sun.javafx.scene.control.ControlAcceleratorSupport
import javafx.fxml.FXML
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.logic.concurrency.JavaFXExecutorService
import nebulosa.desktop.logic.image.ImageManager
import nebulosa.desktop.view.image.Drawable
import nebulosa.desktop.view.image.ImageView
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.camera.Camera
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.IntBuffer

class ImageWindow(override val camera: Camera? = null) : AbstractWindow("Image", "nebulosa-image"), ImageView {

    @Autowired private lateinit var javaFXExecutorService: JavaFXExecutorService

    @FXML private lateinit var fitsImageViewer: ImageViewer
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var scnrMenuItem: MenuItem
    @FXML private lateinit var fitsHeaderMenuItem: MenuItem
    @FXML private lateinit var crosshairCheckMenuItem: CheckMenuItem
    @FXML private lateinit var annotateCheckMenuItem: CheckMenuItem

    @Volatile private var fitsBuffer = IntArray(0)

    private val imageManager = ImageManager(this)

    init {
        title = "Image"
    }

    override fun onCreate() {
        fitsImageViewer.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                if (!maximized) {
                    imageManager.adjustSceneSizeToFitImage(false)
                    fitsImageViewer.resetZoom()
                }
            } else if (it.button == MouseButton.PRIMARY) {
                menu.hide()
                it.consume()
            }
        }

        with(fitsImageViewer) {
            setOnContextMenuRequested { menu.show(this, it.screenX, it.screenY) }
            ControlAcceleratorSupport.addAcceleratorsIntoScene(menu.items, this)
        }

        annotateCheckMenuItem.disableProperty().bind(imageManager.calibration.isNull)
    }

    override fun onStart() {
        imageManager.loadPreferences()
    }

    override fun onStop() {
        fitsBuffer = IntArray(0)

        imageManager.close()
    }

    override val fits
        get() = imageManager.transformedFits ?: imageManager.fits

    override val shadow
        get() = imageManager.shadow

    override val highlight
        get() = imageManager.highlight

    override val midtone
        get() = imageManager.midtone

    override val mirrorHorizontal
        get() = imageManager.mirrorHorizontal

    override val mirrorVertical
        get() = imageManager.mirrorVertical

    override val invert
        get() = imageManager.invert

    override val scnrEnabled
        get() = imageManager.scnrEnabled

    override val scnrChannel
        get() = imageManager.scnrChannel

    override val scnrProtectionMode
        get() = imageManager.scnrProtectionMode

    override val scnrAmount
        get() = imageManager.scnrAmount

    override var hasScnr
        get() = !scnrMenuItem.isDisable
        set(value) {
            scnrMenuItem.isDisable = !value
        }

    override var crosshairEnabled
        get() = crosshairCheckMenuItem.isSelected
        set(value) {
            crosshairCheckMenuItem.isSelected = value
        }

    @FXML
    private fun save() {
        imageManager.save()
    }

    @FXML
    private fun plateSolve() {
        imageManager.plateSolve()
    }

    @FXML
    private fun openImageStretcher() {
        imageManager.openImageStretcher()
    }

    @FXML
    private fun openSCNR() {
        imageManager.openSCNR()
    }

    @FXML
    private fun openFitsHeader() {
        imageManager.openFitsHeader()
    }

    @FXML
    private fun mirrorHorizontal() {
        imageManager.mirrorHorizontal()
    }

    @FXML
    private fun mirrorVertical() {
        imageManager.mirrorVertical()
    }

    @FXML
    private fun invert() {
        imageManager.invert()
    }

    @FXML
    private fun toggleCrosshair() {
        imageManager.toggleCrosshair()
    }

    @FXML
    private fun toggleAnnotation() {
        imageManager.toggleAnnotation()
    }

    fun open(file: File) {
        imageManager.open(file)
    }

    fun open(fits: Image, file: File? = null) {
        imageManager.open(fits, file)
    }

    override fun adjustSceneToImage() {
        javaFXExecutorService.execute {
            fitsImageViewer.resetZoom()
            imageManager.adjustSceneSizeToFitImage()
        }
    }

    override fun draw(fits: Image) {
        val area = fits.width * fits.height

        if (area > fitsBuffer.size) {
            fitsBuffer = IntArray(area)
        }

        fits.writeTo(fitsBuffer)

        val buffer = IntBuffer.wrap(fitsBuffer, 0, area)
        val pixelBuffer = PixelBuffer(fits.width, fits.height, buffer, PixelFormat.getIntArgbPreInstance())
        val writableImage = WritableImage(pixelBuffer)
        fitsImageViewer.load(writableImage)
    }

    override fun scnr(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    ) {
        imageManager.transformImage(
            scnrEnabled = enabled, scnrChannel = channel,
            scnrProtectionMode = protectionMethod,
            scnrAmount = amount,
        )
    }

    override fun stf(shadow: Float, highlight: Float, midtone: Float) {
        imageManager.transformImage(shadow = shadow, highlight = highlight, midtone = midtone)
    }

    override fun redraw() {
        fitsImageViewer.redraw()
    }

    override fun addFirst(element: Drawable) {
        fitsImageViewer.addFirst(element)
    }

    override fun addLast(element: Drawable) {
        fitsImageViewer.addLast(element)
    }

    override fun remove(element: Drawable): Boolean {
        return fitsImageViewer.remove(element)
    }

    override fun removeFirst(): Drawable {
        return fitsImageViewer.removeFirst()
    }

    override fun removeLast(): Drawable {
        return fitsImageViewer.removeLast()
    }

    override fun removeAll(elements: Collection<Drawable>): Boolean {
        return fitsImageViewer.removeAll(elements.toSet())
    }

    override fun removeAll() {
        fitsImageViewer.clear()
    }

    override fun iterator(): Iterator<Drawable> {
        return fitsImageViewer.iterator()
    }

    @Service
    class Opener {

        @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory

        private val windows = hashSetOf<ImageWindow>()

        fun open(fits: Image?, file: File?, camera: Camera? = null): ImageWindow {
            val window = windows
                .firstOrNull { if (camera == null) it.camera == null && !it.showing else it.camera === camera }
                ?: ImageWindow(camera).also { beanFactory.autowireBean(it); beanFactory.autowireBean(it.imageManager) }

            windows.add(window)

            window.show()

            if (fits != null) window.open(fits, file)
            else if (file != null) window.open(file)
            else throw IllegalArgumentException("fits or file parameter must be provided")

            return window
        }
    }
}
