package nebulosa.desktop.imageviewer

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import nebulosa.desktop.core.beans.on
import nebulosa.desktop.core.scene.Screen
import nebulosa.imaging.FitsImage
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.indi.devices.cameras.Camera
import nom.tam.fits.Fits
import java.io.File
import java.nio.IntBuffer
import kotlin.math.max
import kotlin.math.min

class ImageViewerScreen(val camera: Camera? = null) : Screen("ImageViewer", "nebulosa-image-viewer"), ChangeListener<Number> {

    @FXML private lateinit var image: Canvas
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var scnr: MenuItem

    @Volatile @JvmField internal var fits: Image? = null
    @Volatile @JvmField internal var transformedFits: Image? = null

    @Volatile private var buffer = IntArray(0)
    @Volatile private var scale = 1f
    @Volatile private var scaleFactor = 0
    @Volatile private var startX = 0
    @Volatile private var startY = 0
    @Volatile private var dragging = false
    @Volatile private var dragStartX = 0.0
    @Volatile private var dragStartY = 0.0
    @Volatile private var lastDrawTime = 0L

    private val imageStretcherScreen = ImageStretcherScreen(this)
    private val scnrScreen = SCNRScreen(this)

    var shadow = 0f
        private set

    var highlight = 1f
        private set

    var midtone = 0.5f
        private set

    var mirrorHorizontal = false
        private set

    var mirrorVertical = false
        private set

    var invert = false
        private set

    var scnrEnabled = false
        private set

    var scnrChannel = ImageChannel.GREEN
        private set

    var scnrProtectionMode = ProtectionMethod.AVERAGE_NEUTRAL
        private set

    var scnrAmount = 0.5f
        private set

    init {
        setTitleFromCameraAndFile()
    }

    override fun onCreate() {
        image.addEventFilter(ScrollEvent.SCROLL) {
            if (it.deltaX != 0.0 || it.deltaY != 0.0) {
                zoomWithWheel(it)
                it.consume()
            }
        }

        image.addEventFilter(MouseEvent.MOUSE_DRAGGED) {
            if (it.button == MouseButton.PRIMARY) {
                if (!dragging) {
                    dragStartX = it.x
                    dragStartY = it.y
                    dragging = true
                    image.cursor = Cursor.MOVE
                } else {
                    val deltaX = (it.x - dragStartX).toInt()
                    val deltaY = (it.y - dragStartY).toInt()

                    startX -= deltaX
                    startY -= deltaY

                    startX = max(0, startX)
                    startY = max(0, startY)

                    dragStartX = it.x
                    dragStartY = it.y

                    draw()
                }

                it.consume()
            }
        }

        image.addEventFilter(MouseEvent.MOUSE_RELEASED) {
            dragging = false
            image.cursor = Cursor.DEFAULT
        }

        image.parent.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY) {
                menu.hide()
                it.consume()
            }
        }

        image.parent.setOnContextMenuRequested {
            menu.show(image.parent, it.screenX, it.screenY)
        }

        if (camera != null) {
            preferences.double("imageViewer.${camera.name}.screen.x")?.let { x = it }
            preferences.double("imageViewer.${camera.name}.screen.y")?.let { y = it }

            xProperty().on { preferences.double("imageViewer.${camera.name}.screen.x", it) }
            yProperty().on { preferences.double("imageViewer.${camera.name}.screen.y", it) }
        } else {
            preferences.double("imageViewer.screen.x")?.let { x = it }
            preferences.double("imageViewer.screen.y")?.let { y = it }

            xProperty().on { preferences.double("imageViewer.screen.x", it) }
            yProperty().on { preferences.double("imageViewer.screen.y", it) }
        }
    }

    override fun onStart() {
        shadow = 0f
        highlight = 1f
        midtone = 0.5f
        mirrorHorizontal = false
        mirrorVertical = false
        invert = false

        if (camera != null) {
            preferences.double("imageViewer.${camera.name}.screen.x")?.let { x = it }
            preferences.double("imageViewer.${camera.name}.screen.y")?.let { y = it }
        } else {
            preferences.double("imageViewer.screen.x")?.let { x = it }
            preferences.double("imageViewer.screen.y")?.let { y = it }
        }
    }

    override fun onStop() {
        fits = null
        transformedFits = null

        buffer = IntArray(0)
        scale = 1f
        scaleFactor = 0
        startX = 0
        startY = 0
        dragging = false
        dragStartX = 0.0
        dragStartY = 0.0

        imageStretcherScreen.close()
        scnrScreen.close()

        System.gc()
    }

    private fun setTitleFromCameraAndFile(file: File? = null) {
        title = buildString(64) {
            append("Image")
            if (camera != null) append(" · ${camera.name}")
            if (file != null) append(" · ${file.name}")
        }
    }

    private fun zoomWithWheel(event: ScrollEvent) {
        val delta = if (event.deltaY == 0.0 && event.deltaX != 0.0) event.deltaX else event.deltaY
        val wheel = if (delta < 0) -1 else 1
        val scaleFactor = max(0, min(scaleFactor + wheel, SCALE_FACTORS.size - 1))

        if (scaleFactor != this.scaleFactor) {
            this.scaleFactor = scaleFactor
            val newScale = SCALE_FACTORS[scaleFactor]
            zoomToPoint(newScale, event.x, event.y)
        }
    }

    private fun zoomToPoint(
        newScale: Double,
        pointX: Double, pointY: Double,
    ) {
        val bounds = image.parent.boundsInLocal

        val x = ((startX + pointX) / bounds.width) * (bounds.width * newScale)
        val y = ((startY + pointY) / bounds.height) * (bounds.height * newScale)

        val toX = (x / newScale - x / scale + x * newScale) / newScale
        val toY = (y / newScale - y / scale + y * newScale) / newScale

        scale = newScale.toFloat()

        if (scale == 1f) {
            startX = 0
            startY = 0
        } else {
            startX -= ((toX - x) * scale).toInt()
            startY -= ((toY - y) * scale).toInt()

            startX = max(0, startX)
            startY = max(0, startY)
        }

        draw()
    }

    override fun changed(
        observable: ObservableValue<out Number>,
        oldValue: Number, newValue: Number,
    ) {
        if (observable === widthProperty()) {
            val factor = newValue.toDouble() / oldValue.toDouble()
            startX = (startX * factor).toInt()
            startY = (startY * factor).toInt()
        }

        draw()
    }

    @Synchronized
    fun open(file: File) {
        setTitleFromCameraAndFile(file)

        showAndFocus()

        val fits = FitsImage(Fits(file))
        fits.read()
        this.fits = fits
        transformedFits = fits.clone()

        scnr.isDisable = fits.mono

        widthProperty().removeListener(this)
        heightProperty().removeListener(this)

        val factor = fits.width.toFloat() / fits.height.toFloat()
        val titleHeight = height - scene.height

        if (factor >= 1) {
            width = 640.0
            height = 640.0 / factor + titleHeight - 1
        } else {
            width = 640.0 / factor
            height = 640.0 + titleHeight - 1
        }

        widthProperty().addListener(this)
        heightProperty().addListener(this)

        transformImage()
        draw()
    }

    fun transformImage(
        shadow: Float = this.shadow, highlight: Float = this.highlight, midtone: Float = this.midtone,
        mirrorHorizontal: Boolean = this.mirrorHorizontal, mirrorVertical: Boolean = this.mirrorVertical,
        invert: Boolean = this.invert,
        scnrEnabled: Boolean = this.scnrEnabled, scnrChannel: ImageChannel = this.scnrChannel,
        scnrProtectionMode: ProtectionMethod = this.scnrProtectionMode,
        scnrAmount: Float = this.scnrAmount,
    ) {
        this.shadow = shadow
        this.highlight = highlight
        this.midtone = midtone
        this.mirrorHorizontal = mirrorHorizontal
        this.mirrorVertical = mirrorVertical
        this.invert = invert
        this.scnrEnabled = scnrEnabled
        this.scnrChannel = scnrChannel
        this.scnrProtectionMode = scnrProtectionMode
        this.scnrAmount = scnrAmount

        transformImage()
        draw()
    }

    private fun transformImage() {
        if (!canDraw()) return

        // TODO: How to handle rotation transformation if data is copy but width/height is not?
        // TODO: Reason: Image will be rotated for each draw.
        fits!!.data.copyInto(transformedFits!!.data)

        val algorithms = arrayListOf<TransformAlgorithm>()
        algorithms.add(Flip(mirrorHorizontal, mirrorVertical))
        if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
        algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
        if (invert) algorithms.add(Invert)

        transformedFits = TransformAlgorithm.of(algorithms).transform(transformedFits!!)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun canDraw(): Boolean {
        return (System.currentTimeMillis() - lastDrawTime) >= 10L
    }

    private fun draw() {
        if (!canDraw()) return

        lastDrawTime = System.currentTimeMillis()

        val fits = transformedFits ?: return

        val areaWidth = scene.width.toInt()
        val areaHeight = scene.height.toInt()
        val area = areaWidth * areaHeight

        image.width = scene.width
        image.height = scene.height

        if (area > buffer.size) {
            buffer = IntArray(area)
        }

        val factorW = fits.width.toFloat() / areaWidth
        val factorH = fits.height.toFloat() / areaHeight
        val factor = max(factorW, factorH) / scale

        var prevIndex = -1
        var prevColor = 0
        var idx = 0

        for (y in 0 until areaHeight) {
            for (x in 0 until areaWidth) {
                val realX = ((startX + x) * factor).toInt()
                val realY = ((startY + y) * factor).toInt()

                if (realX < 0 || realY < 0 || realX >= fits.width || realY >= fits.height) {
                    buffer[idx++] = 0
                    continue
                }

                val index = realY * fits.stride + realX * fits.pixelStride

                if (prevIndex == index) {
                    buffer[idx++] = prevColor
                } else if (fits.mono) {
                    val c = (fits.data[index] * 255f).toInt()
                    prevColor = 0xFF000000.toInt() or (c shl 16) or (c shl 8) or c
                    buffer[idx++] = prevColor
                } else {
                    val a = (fits.data[index] * 255f).toInt()
                    val b = (fits.data[index + 1] * 255f).toInt()
                    val c = (fits.data[index + 2] * 255f).toInt()
                    prevColor = 0xFF000000.toInt() or (a shl 16) or (b shl 8) or c
                    buffer[idx++] = prevColor
                }

                prevIndex = index
            }
        }

        val intBuffer = IntBuffer.wrap(buffer, 0, idx)
        val pixelBuffer = PixelBuffer(areaWidth, areaHeight, intBuffer, PixelFormat.getIntArgbPreInstance())
        val writableImage = WritableImage(pixelBuffer)

        val g = image.graphicsContext2D
        g.clearRect(0.0, 0.0, image.width, image.height)
        g.drawImage(writableImage, 0.0, 0.0)

        imageStretcherScreen.drawHistogram()
    }

    @FXML
    private fun openImageStretcher() {
        imageStretcherScreen.showAndFocus()
    }

    @FXML
    private fun openSCNR() {
        scnrScreen.showAndFocus()
    }

    @FXML
    private fun mirrorHorizontal() {
        transformImage(mirrorHorizontal = !mirrorHorizontal)
    }

    @FXML
    private fun mirrorVertical() {
        transformImage(mirrorVertical = !mirrorVertical)
    }

    @FXML
    private fun invert() {
        transformImage(invert = !invert)
    }

    companion object {

        @JvmStatic private val SCALE_FACTORS = doubleArrayOf(
            1.0, 1.25, 1.5, 1.75, 2.0, 2.5,
            3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
            10.0, 15.0, 20.0, 25.0, 50.0, 75.0, 100.0, 200.0, 500.0,
        )
    }
}
