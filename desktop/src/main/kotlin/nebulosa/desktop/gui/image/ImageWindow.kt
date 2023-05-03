package nebulosa.desktop.gui.image

import com.sun.javafx.scene.control.ControlAcceleratorSupport
import javafx.animation.PauseTransition
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.util.Duration
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.gui.control.CopyableLabel
import nebulosa.desktop.gui.control.ImageViewer
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.logic.asBoolean
import nebulosa.desktop.logic.image.ImageManager
import nebulosa.desktop.logic.or
import nebulosa.desktop.view.image.ImageView
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.camera.Camera
import nebulosa.math.AngleFormatter
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.HasAxisSize
import nebulosa.skycatalog.SkyObject
import net.kurobako.gesturefx.AffineEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.stereotype.Service
import java.io.File
import java.nio.IntBuffer
import kotlin.jvm.optionals.getOrNull

class ImageWindow(override val camera: Camera? = null) : AbstractWindow("Image", "image"), ImageView {

    @FXML private lateinit var imageViewer: ImageViewer
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
    @FXML private lateinit var zoomFactorLabel: Label

    @Volatile private var imageData = IntArray(0)

    private val imageSecondaryClickLocation = SimpleObjectProperty<Point2D>()
    private val transformer = PauseTransition(Duration.seconds(0.5))
    private val imageManager = ImageManager(this)
    private val zoomTextTransition = PauseTransition(Duration.seconds(2.0))

    init {
        title = "Image"

        transformer.setOnFinished {
            launch { imageManager.transformAndDraw() }
        }

        zoomTextTransition.setOnFinished {
            zoomFactorLabel.isVisible = false
        }
    }

    override fun onCreate() {
        imageViewer.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                if (!maximized) {
                    launch {
                        imageManager.adjustSceneSizeToFitImage(false)
                        imageViewer.resetZoom()
                    }
                }
            } else if (it.button == MouseButton.PRIMARY) {
                menu.hide()
                it.consume()
            }
        }

        imageViewer.addEventFilter(AffineEvent.CHANGED) {
            val prev = it.previous().getOrNull() ?: return@addEventFilter

            if (prev.mxx != it.current().mxx) {
                zoomFactorLabel.text = "%.2fx".format(imageViewer.currentScale)
                zoomFactorLabel.isVisible = true
                zoomTextTransition.playFromStart()
            }
        }

        with(imageViewer) {
            setOnContextMenuRequested {
                menu.show(this, it.screenX, it.screenY)

                val targetPoint = imageViewer.targetPointAt(Point2D(it.x, it.y)).getOrNull()
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
        launch { imageManager.solve() }
    }

    @FXML
    private fun blindSolve() {
        launch { imageManager.solve(true) }
    }

    @FXML
    private fun openImageStretcher() {
        imageManager.openImageStretcher()
    }

    @FXML
    private fun autoStretch() {
        launch { imageManager.autoStretch() }
    }

    @FXML
    private fun openSCNR() {
        imageManager.openSCNR()
    }

    @FXML
    private fun openFitsHeader() {
        launch { imageManager.openFitsHeader() }
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
        launch { imageManager.toggleCrosshair() }
    }

    @FXML
    private fun toggleAnnotation() {
        launch { imageManager.toggleAnnotation() }
    }

    @FXML
    private fun toggleAnnotationOptions() {
        imageManager.toggleAnnotationOptions()
    }

    @FXML
    private fun pointMountHere() {
        val target = imageSecondaryClickLocation.get() ?: return
        imageManager.pointMountHere(target.x, target.y)
        imageSecondaryClickLocation.set(null)
    }

    override suspend fun open(
        file: File,
        resetTransformation: Boolean,
        calibration: Calibration?,
        title: String?,
    ) = withMain {
        imageManager.open(file, resetTransformation, calibration, title)
    }

    override suspend fun open(
        fits: Image, file: File?,
        resetTransformation: Boolean,
        calibration: Calibration?,
        title: String?,
    ) = withMain {
        imageManager.open(fits, file, resetTransformation, calibration, title)
    }

    override suspend fun adjustSceneToImage() {
        imageViewer.resetZoom()
        imageManager.adjustSceneSizeToFitImage()
    }

    override suspend fun draw(image: Image) = withIO {
        val area = image.width * image.height

        if (area > imageData.size) {
            imageData = IntArray(area)
        }

        image.writeTo(imageData)

        val buffer = IntBuffer.wrap(imageData, 0, area)
        val pixelBuffer = PixelBuffer(image.width, image.height, buffer, PixelFormat.getIntArgbPreInstance())
        val writableImage = WritableImage(pixelBuffer)

        withMain { imageViewer.load(writableImage) }
    }

    override suspend fun scnr(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Float,
    ) = withMain {
        imageManager.transformImage(
            scnrEnabled = enabled, scnrChannel = channel,
            scnrProtectionMode = protectionMethod,
            scnrAmount = amount,
        )
    }

    override suspend fun stf(shadow: Float, highlight: Float, midtone: Float) = withMain {
        imageManager.transformImage(shadow = shadow, highlight = highlight, midtone = midtone)
    }

    override fun transformAndDraw() {
        transformer.playFromStart()
    }

    override fun showStarInfo(star: SkyObject) {
        showAlert {
            val box = VBox()
            header = CopyableLabel(star.names)
            header.styleClass.add("text-bold")
            val rightAscension = star.rightAscension.format(AngleFormatter.HMS)
            val declination = star.declination.format(AngleFormatter.SIGNED_DMS)
            box.children.add(CopyableLabel("RA: $rightAscension DEC: $declination"))
            if (star.magnitude < SkyObject.UNKNOWN_MAGNITUDE) box.children.add(CopyableLabel("MAGNITUDE: %.2f".format(star.magnitude)))
            box.children.add(CopyableLabel("TYPE: ${star.type.description}"))
            if (star.distance > 0.0) box.children.add(CopyableLabel("DISTANCE: %.1f ly".format(star.distance)))
            if (star is HasAxisSize && (star.majorAxis.value > 0.0 || star.minorAxis.value > 0.0)) {
                val label = CopyableLabel("SIZE (arcmin): %.2f x %.2f".format(star.minorAxis.arcmin, star.majorAxis.arcmin))
                box.children.add(label)
            }
            box.children.add(CopyableLabel("CONSTELLATION: ${star.constellation.latinName} (${star.constellation.iau})"))
            minWidth = 380.0
            content = box
        }
    }

    override fun redraw() {
        imageViewer.redraw()
    }

    override fun addFirst(shape: Node) {
        imageViewer.addFirst(shape)
    }

    override fun addLast(shape: Node) {
        imageViewer.addLast(shape)
    }

    override fun remove(shape: Node) {
        imageViewer.remove(shape)
    }

    override fun removeFirst(): Node? {
        return imageViewer.removeFirst()
    }

    override fun removeLast(): Node? {
        return imageViewer.removeLast()
    }

    override fun registerMouseListener(listener: ImageViewer.MouseListener) {
        imageViewer.registerMouseListener(listener)
    }

    override fun unregisterMouseListener(listener: ImageViewer.MouseListener) {
        imageViewer.unregisterMouseListener(listener)
    }

    @Service
    class Opener : ImageView.Opener {

        @Autowired private lateinit var beanFactory: AutowireCapableBeanFactory

        private val windows = hashSetOf<ImageWindow>()
        private val windowsMap = hashMapOf<Any, ImageWindow>()

        override suspend fun open(
            image: Image?, file: File?,
            token: Any?, resetTransformation: Boolean,
            calibration: Calibration?,
            title: String?,
        ): ImageView {
            val window = withMain {
                if (token != null) {
                    windowsMap[token] ?: ImageWindow(if (token is Camera) token else null)
                } else {
                    windows.firstOrNull { !it.showing } ?: ImageWindow()
                }
            }

            beanFactory.autowireBean(window)
            beanFactory.autowireBean(window.imageManager)

            if (token != null) windowsMap[token] = window
            else windows.add(window)

            withMain {
                window.show()

                if (image != null) window.open(image, file, resetTransformation, calibration, title)
                else if (file != null) window.open(file, true, calibration, title)
                else LOG.error("fits or file parameter must be provided")
            }

            return window
        }

        companion object {

            @JvmStatic private val LOG = LoggerFactory.getLogger(Opener::class.java)
        }
    }
}
