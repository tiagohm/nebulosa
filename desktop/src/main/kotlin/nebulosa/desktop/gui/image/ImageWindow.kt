package nebulosa.desktop.gui.image

import com.sun.javafx.scene.control.ControlAcceleratorSupport
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.geometry.Point2D
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
import nebulosa.desktop.logic.asBoolean
import nebulosa.desktop.logic.image.ImageManager
import nebulosa.desktop.logic.or
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
import kotlin.jvm.optionals.getOrNull

class ImageWindow(override val camera: Camera? = null) : AbstractWindow("Image", "image"), ImageView {

    @FXML private lateinit var fitsImageViewer: ImageViewer
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var solveMenuItem: MenuItem
    @FXML private lateinit var blindSolveMenuItem: MenuItem
    @FXML private lateinit var autoStretchCheckMenuItem: CheckMenuItem
    @FXML private lateinit var mirrorHorizontalCheckMenuItem: CheckMenuItem
    @FXML private lateinit var mirrorVerticalCheckMenuItem: CheckMenuItem
    @FXML private lateinit var invertCheckMenuItem: CheckMenuItem
    @FXML private lateinit var scnrMenuItem: MenuItem
    @FXML private lateinit var pointMountHereMenuItem: MenuItem
    @FXML private lateinit var fitsHeaderMenuItem: MenuItem
    @FXML private lateinit var crosshairCheckMenuItem: CheckMenuItem
    @FXML private lateinit var annotateCheckMenuItem: CheckMenuItem

    @Volatile private var imageData = IntArray(0)
    private val imageSecondaryClickLocation = SimpleObjectProperty<Point2D>()

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
            setOnContextMenuRequested {
                menu.show(this, it.screenX, it.screenY)

                val targetPoint = fitsImageViewer.targetPointAt(Point2D(it.x, it.y)).getOrNull()
                imageSecondaryClickLocation.set(targetPoint)
            }

            ControlAcceleratorSupport.addAcceleratorsIntoScene(menu.items, this)
        }

        solveMenuItem.disableProperty().bind(imageManager.file.isNull)
        blindSolveMenuItem.disableProperty().bind(solveMenuItem.disableProperty())

        annotateCheckMenuItem.disableProperty()
            .bind(imageManager.calibration.asBoolean { it == null || !it.hasWCS })

        pointMountHereMenuItem.disableProperty().bind(
            annotateCheckMenuItem.disableProperty() or imageSecondaryClickLocation.isNull
                    or !imageManager.mountProperty.connectedProperty
        )
    }

    override fun onStart() {
        imageManager.initialize()
        imageManager.loadPreferences()
    }

    override fun onStop() {
        imageData = IntArray(0)

        imageManager.close()
    }

    override val originalImage
        get() = imageManager.image

    override val transformedImage
        get() = imageManager.transformedImage

    override val shadow
        get() = imageManager.shadow

    override val highlight
        get() = imageManager.highlight

    override val midtone
        get() = imageManager.midtone

    override var mirrorHorizontal
        get() = mirrorHorizontalCheckMenuItem.isSelected
        set(value) {
            mirrorHorizontalCheckMenuItem.isSelected = value
        }

    override var mirrorVertical
        get() = mirrorVerticalCheckMenuItem.isSelected
        set(value) {
            mirrorVerticalCheckMenuItem.isSelected = value
        }

    override var invert
        get() = invertCheckMenuItem.isSelected
        set(value) {
            invertCheckMenuItem.isSelected = value
        }

    override val autoStretchEnabled
        get() = autoStretchCheckMenuItem.isSelected

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

    override val crosshairEnabled
        get() = crosshairCheckMenuItem.isSelected

    override val annotationEnabled
        get() = annotateCheckMenuItem.isSelected

    @FXML
    private fun save() {
        imageManager.save()
    }

    @FXML
    private fun solve() {
        imageManager.solve(false)
    }

    @FXML
    private fun blindSolve() {
        imageManager.solve(true)
    }

    @FXML
    private fun openImageStretcher() {
        imageManager.openImageStretcher()
    }

    @FXML
    private fun autoStretch() {
        imageManager.autoStretch()
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

    @FXML
    private fun toggleAnnotationOptions() {
        imageManager.toggleAnnotationOptions()
    }

    @FXML
    private fun pointMountHere() {
        imageManager.pointMountHere(imageSecondaryClickLocation.get() ?: return)
        imageSecondaryClickLocation.set(null)
    }

    override fun open(file: File, resetTransformation: Boolean) {
        imageManager.open(file, resetTransformation)
    }

    override fun open(fits: Image, file: File?, resetTransformation: Boolean) {
        imageManager.open(fits, file, resetTransformation)

        annotateCheckMenuItem.isSelected = false
    }

    override fun adjustSceneToImage() {
        javaFXExecutorService.execute {
            fitsImageViewer.resetZoom()
            imageManager.adjustSceneSizeToFitImage()
        }
    }

    override fun draw(image: Image) {
        val area = image.width * image.height

        if (area > imageData.size) {
            imageData = IntArray(area)
        }

        image.writeTo(imageData)

        val buffer = IntBuffer.wrap(imageData, 0, area)
        val pixelBuffer = PixelBuffer(image.width, image.height, buffer, PixelFormat.getIntArgbPreInstance())
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

    fun updateAnnotationOptions(
        namedStars: Boolean, messier: Boolean, ngc: Boolean,
        hip: Boolean, tycho: Boolean, sao: Boolean,
        planets: Boolean, asteroids: Boolean,
    ) {

    }

    override fun redraw() {
        Platform.runLater { fitsImageViewer.redraw() }
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
    class Opener : ImageView.Opener {

        @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory

        private val windows = hashSetOf<ImageWindow>()
        private val windowsMap = hashMapOf<Any, ImageWindow>()

        override fun open(
            image: Image?, file: File?,
            token: Any?, resetTransformation: Boolean,
        ): ImageView {
            val window = if (token != null) {
                windowsMap[token] ?: ImageWindow(if (token is Camera) token else null)
            } else {
                windows.firstOrNull { !it.showing } ?: ImageWindow(null)
            }

            beanFactory.autowireBean(window)
            beanFactory.autowireBean(window.imageManager)

            if (token != null) windowsMap[token] = window
            else windows.add(window)

            window.show()

            if (image != null) window.open(image, file, resetTransformation)
            else if (file != null) window.open(file, resetTransformation = true)
            else throw IllegalArgumentException("fits or file parameter must be provided")

            return window
        }
    }
}
