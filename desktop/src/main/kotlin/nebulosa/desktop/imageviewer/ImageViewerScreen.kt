package nebulosa.desktop.imageviewer

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
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
import nebulosa.imaging.ExtendedImage
import nebulosa.imaging.FitsImage
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.*
import nebulosa.indi.devices.cameras.Camera
import nom.tam.fits.Fits
import java.io.Closeable
import java.io.File
import java.nio.IntBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class ImageViewerScreen(val camera: Camera? = null) : Screen("ImageViewer", "nebulosa-image-viewer") {

    @FXML private lateinit var image: Canvas
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var scnr: MenuItem
    @FXML private lateinit var fitsHeader: MenuItem

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

    private val imageStretcherScreen = ImageStretcherScreen(this)
    private val scnrScreen = SCNRScreen(this)
    private val fitsHeaderScreen = FITSHeaderScreen()

    private val screenBounds = javafx.stage.Screen.getPrimary().bounds
    private val transformPublisher = BehaviorSubject.create<Unit>()

    @Volatile private var borderSize = 0.0
    @Volatile private var titleHeight = 0.0
    @Volatile private var idealSceneWidth = 640.0
    @Volatile private var idealSceneHeight = 640.0
    @Volatile private var transformSubscriber: Disposable? = null

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
            synchronized(this) {
                if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                    startX = 0
                    startY = 0
                    scaleFactor = 0
                    scale = 1f

                    if (isMaximized) {
                        isMaximized = false
                        adjustSceneSizeToFitImage(true)
                    } else {
                        adjustSceneSizeToFitImage(false)
                    }

                    draw()
                } else if (it.button == MouseButton.PRIMARY) {
                    menu.hide()
                    it.consume()
                }
            }
        }

        image.parent.setOnContextMenuRequested {
            menu.show(image.parent, it.screenX, it.screenY)
        }

        if (camera != null) {
            preferences.double("imageViewer.equipment.${camera.name}.screen.x")?.let { x = it }
            preferences.double("imageViewer.equipment.${camera.name}.screen.y")?.let { y = it }

            xProperty().on { preferences.double("imageViewer.equipment.${camera.name}.screen.x", it) }
            yProperty().on { preferences.double("imageViewer.equipment.${camera.name}.screen.y", it) }
        } else {
            preferences.double("imageViewer.screen.x")?.let { x = it }
            preferences.double("imageViewer.screen.y")?.let { y = it }

            xProperty().on { preferences.double("imageViewer.screen.x", it) }
            yProperty().on { preferences.double("imageViewer.screen.y", it) }
        }

        widthProperty().on(::widthChanged)
        heightProperty().on(::heightChanged)
    }

    override fun onStart() {
        val area = (screenBounds.width * screenBounds.height).toInt()
        buffer = IntArray(area)

        borderSize = (width - scene.width) / 2.0
        titleHeight = (height - scene.height) - borderSize

        transformSubscriber = transformPublisher
            .debounce(500L, TimeUnit.MILLISECONDS)
            .subscribe {
                transformImage()
                draw()
                imageStretcherScreen.drawHistogram()
            }

        shadow = 0f
        highlight = 1f
        midtone = 0.5f
        mirrorHorizontal = false
        mirrorVertical = false
        invert = false

        if (camera != null) {
            preferences.double("imageViewer.equipment.${camera.name}.screen.x")?.let { x = it }
            preferences.double("imageViewer.equipment.${camera.name}.screen.y")?.let { y = it }
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
        fitsHeaderScreen.close()

        transformSubscriber?.dispose()
        transformSubscriber = null

        System.gc()
    }

    private fun setTitleFromCameraAndFile(file: File? = null) {
        title = buildString(64) {
            append("Image")
            if (camera != null) append(" · ${camera.name}")
            if (file != null) append(" · ${file.name}")
        }
    }

    private fun widthChanged(value: Double) {
        if (sizeChanged(value, true)) {
            draw()

            if (camera != null) {
                preferences.double("imageViewer.equipment.${camera.name}.screen.width", value)
            } else {
                preferences.double("imageViewer.screen.width", value)
            }
        }
    }

    private fun heightChanged(value: Double) {
        if (sizeChanged(value, false)) {
            draw()

            if (camera != null) {
                preferences.double("imageViewer.equipment.${camera.name}.screen.height", value)
            } else {
                preferences.double("imageViewer.screen.height", value)
            }
        }
    }

    private fun sizeChanged(value: Double, isWidth: Boolean): Boolean {
        val fits = fits ?: return false

        if (value <= 0.0) return false

        val factor = fits.width.toDouble() / fits.height.toDouble()

        if (isWidth && factor >= 1.0) {
            idealSceneWidth = value
            idealSceneHeight = value / factor
        } else if (!isWidth && factor < 1.0) {
            val valueMinusTitleHeight = value - titleHeight
            idealSceneHeight = valueMinusTitleHeight
            idealSceneWidth = valueMinusTitleHeight * factor
        } else {
            return false
        }

        image.width = idealSceneWidth
        image.height = idealSceneHeight

        return true
    }

    private fun adjustSceneSizeToFitImage(defaultSize: Boolean) {
        val fits = fits ?: return

        val factor = fits.width.toDouble() / fits.height.toDouble()

        val defaultWidth = (if (camera != null) preferences.double("imageViewer.equipment.${camera.name}.screen.width")
        else preferences.double("imageViewer.screen.width")) ?: (screenBounds.width / 2)

        val defaultHeight = (if (camera != null) preferences.double("imageViewer.equipment.${camera.name}.screen.height")
        else preferences.double("imageViewer.screen.height")) ?: (screenBounds.height / 2)

        val sceneSize = if (factor >= 1.0)
            if (defaultSize) defaultWidth
            else min(screenBounds.width, width)
        else if (defaultSize) defaultHeight - titleHeight
        else min(screenBounds.height, height - titleHeight)

        if (factor >= 1.0) {
            idealSceneWidth = sceneSize
            idealSceneHeight = sceneSize / factor
        } else {
            idealSceneHeight = sceneSize
            idealSceneWidth = sceneSize * factor
        }

        width = idealSceneWidth
        height = idealSceneHeight + titleHeight

        image.width = idealSceneWidth
        image.height = idealSceneHeight
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

        startX -= ((toX - x) * scale).toInt()
        startY -= ((toY - y) * scale).toInt()

        startX = max(0, startX)
        startY = max(0, startY)

//        val areaWidth = scene.width.toInt()
//        val areaHeight = scene.height.toInt()
//
//        val factorW = fits!!.width.toFloat() / areaWidth
//        val factorH = fits!!.height.toFloat() / areaHeight
//        val factor = max(factorW, factorH) / scale
//
//        val maxStartX = (fits!!.width / factor).toInt()
//        val maxStartY = (fits!!.height / factor).toInt()
//
//        val x0 = -startX
//        val y0 = -startY
//
//        val x1 = -startX + maxStartX
//        val y1 = -startY + maxStartY
//
//        val px = image.localToParent(pointX, pointY)
//
//        if (px.x >= x0 && px.x <= x1 && px.y >= y0 && px.y <= y1) {
//            println("dentro $x0 $x1 $y0 $y1")
//        } else {
//            println("fora")
//            startX = 0
//            startY = 0
//        }

        draw()
    }

    @Synchronized
    fun open(file: File) {
        setTitleFromCameraAndFile(file)

        val adjustToDefaultSize = this.fits == null

        val fits = if (file.extension.startsWith("fit")) FitsImage(Fits(file))
        else ExtendedImage(file)

        this.fits = fits
        this.transformedFits = null

        scnr.isDisable = fits.mono
        fitsHeader.isDisable = fits !is FitsImage

        adjustSceneSizeToFitImage(adjustToDefaultSize)

        transformImage()
        draw()
        imageStretcherScreen.drawHistogram()
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

        transformPublisher.onNext(Unit)
    }

    @Synchronized
    private fun transformImage() {
        val shouldBeTransformed = this.shadow != 0f || this.highlight != 1f || this.midtone != 0.5f
                || this.mirrorHorizontal || this.mirrorVertical || this.invert
                || this.scnrEnabled

        // TODO: How to handle rotation transformation if data is copy but width/height is not?
        // TODO: Reason: Image will be rotated for each draw.
        transformedFits = if (shouldBeTransformed) fits!!.clone() else null

        if (transformedFits != null) {
            val algorithms = arrayListOf<TransformAlgorithm>()
            algorithms.add(Flip(mirrorHorizontal, mirrorVertical))
            if (scnrEnabled) algorithms.add(SubtractiveChromaticNoiseReduction(scnrChannel, scnrAmount, scnrProtectionMode))
            algorithms.add(ScreenTransformFunction(midtone, shadow, highlight))
            if (invert) algorithms.add(Invert)

            transformedFits = TransformAlgorithm.of(algorithms).transform(transformedFits!!)
        }
    }

    // TODO: Too slow when window is maximized.
    private fun draw() {
        val fits = transformedFits ?: fits ?: return

        val areaWidth = min(idealSceneWidth, screenBounds.width).toInt()
        val areaHeight = min(idealSceneHeight, screenBounds.height).toInt()

        val factorW = fits.width.toFloat() / areaWidth
        val factorH = fits.height.toFloat() / areaHeight
        val factor = max(factorW, factorH) / scale

        val maxStartX = (fits.width / factor).toInt()
        val maxStartY = (fits.height / factor).toInt()

        // Prevent move to left/up.
        if (-startX < 0 || -startY < 0) {
            if (maxStartX - startX <= scene.width.toInt()) {
                startX = maxStartX - scene.width.toInt()
            }
            if (maxStartY - startY <= scene.height.toInt()) {
                startY = maxStartY - scene.height.toInt()
            }
        }

        // Prevent move to right/bottom.
        if (startX > maxStartX) {
            startX = maxStartX
        } else if (startX < 0) {
            startX = 0
        }

        if (startY > maxStartY) {
            startY = maxStartY
        } else if (startY < 0) {
            startY = 0
        }

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
        // g.clearRect(0.0, 0.0, image.width, image.height)
        g.drawImage(writableImage, 0.0, 0.0)
    }

    @FXML
    private fun openImageStretcher() {
        imageStretcherScreen.show(true, true)
    }

    @FXML
    private fun openSCNR() {
        scnrScreen.show(true, true)
    }

    @FXML
    private fun openFITSHeader() {
        val fits = fits ?: return

        if (fits is FitsImage) {
            fitsHeaderScreen.show(bringToFront = true)
            fitsHeaderScreen.load(fits.header)
        }
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

    companion object : Closeable {

        @JvmStatic private val DRAW_EXECUTOR = Executors.newSingleThreadScheduledExecutor()

        @JvmStatic private val SCALE_FACTORS = doubleArrayOf(
            // 0.125, 0.25, 0.5, 0.75,
            1.0, 1.25, 1.5, 1.75, 2.0, 2.5,
            3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
            10.0, 15.0, 20.0, 25.0, 50.0, 75.0, 100.0, 200.0, 500.0,
        )

        override fun close() {
            DRAW_EXECUTOR.shutdownNow()
        }
    }
}
