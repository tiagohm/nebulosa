package nebulosa.desktop.cameras

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.ContextMenu
import javafx.scene.image.ImageView
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import nebulosa.desktop.internal.Window
import nebulosa.imaging.FitsImage
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.ScreenTransformFunction
import nebulosa.imaging.algorithms.TransformAlgorithm
import nebulosa.indi.devices.cameras.Camera
import nom.tam.fits.Fits
import java.io.File
import java.nio.IntBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

class ImageViewer(val camera: Camera? = null) : Window("ImageViewer"), ChangeListener<Number> {

    @FXML private lateinit var image: ImageView
    @FXML private lateinit var menu: ContextMenu

    @Volatile @JvmField internal var fits: Image? = null
    @Volatile private var transformedFits: Image? = null

    @Volatile private var buffer = IntBuffer.allocate(1)
    @Volatile private var bufferSize = 1
    @Volatile private var scale = 1.0
    @Volatile private var startX = 0
    @Volatile private var startY = 0
    @Volatile private var dragging = false
    @Volatile private var dragStartX = 0.0
    @Volatile private var dragStartY = 0.0
    @Volatile private var lastDrawTime = 0L

    private val imageStretcher by lazy { ImageStretcher(this) }

    init {
        setTitleFromCameraAndFile()

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
    }

    var shadow = 0f
        set(value) {
            field = value
            applyTransformAlgorithms()
            draw()
        }

    var highlight = 1f
        set(value) {
            field = value
            applyTransformAlgorithms()
            draw()
        }

    var midtone = 0.5f
        set(value) {
            field = value
            applyTransformAlgorithms()
            draw()
        }

    override fun onStop() {
        fits = null
        image.image = null

        buffer = IntBuffer.allocate(1)
        bufferSize = 1
        scale = 1.0
        startX = 0
        startY = 0
        dragging = false
        dragStartX = 0.0
        dragStartY = 0.0
        lastDrawTime = 0L
    }

    private fun setTitleFromCameraAndFile(file: File? = null) {
        title = "Image"
        if (camera != null) title += " - ${camera.name}"
        if (file != null) title += " - ${file.name}"
    }

    private fun zoomWithWheel(event: ScrollEvent) {
        val delta = if (event.deltaY == 0.0 && event.deltaX != 0.0) event.deltaX else event.deltaY
        val wheel = if (delta < 0) -1 else 1
        val newScale = scale * exp((wheel * 0.25) / 3)
        zoomToPoint(min(max(newScale, 1.0), 150.0), event.x, event.y)
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

        scale = newScale

        if (scale == 1.0) {
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
            scale *= factor
            startX = (startX * factor).toInt()
            startY = (startY * factor).toInt()
        }

        draw()
    }

    @Synchronized
    fun open(file: File) {
        setTitleFromCameraAndFile(file)

        show()

        val fits = FitsImage(Fits(file))
        fits.read()
        this.fits = fits
        transformedFits = fits.clone()

        widthProperty().removeListener(this)
        heightProperty().removeListener(this)

        width = 640.0
        val titleHeight = height - scene.height
        height = fits.height * (width / fits.width) + titleHeight - 1

        widthProperty().addListener(this)
        heightProperty().addListener(this)

        draw()
    }

    private fun applyTransformAlgorithms() {
        fits!!.data.copyInto(transformedFits!!.data)

        val algorithms = TransformAlgorithm.of(
            ScreenTransformFunction(midtone, shadow, highlight),
        )

        transformedFits = algorithms.transform(transformedFits!!)
    }

    private fun draw() {
        val fits = transformedFits ?: return

        val curTime = System.currentTimeMillis()
        if (curTime - lastDrawTime < 100) return
        lastDrawTime = curTime

        val bounds = image.parent.boundsInLocal
        val areaWidth = bounds.width.toInt()
        val areaHeight = bounds.height.toInt()
        val area = areaWidth * areaHeight

        if (area > bufferSize) {
            bufferSize = area
            buffer = IntBuffer.allocate(bufferSize)
        } else {
            buffer.clear()
        }

        val factor = fits.width.toFloat() / areaWidth / scale

        var prevIndex = -1
        var prevColor = 0

        for (y in 0 until areaHeight) {
            for (x in 0 until areaWidth) {
                val realX = ((startX + x) * factor).toInt()
                val realY = ((startY + y) * factor).toInt()

                if (realX < 0 || realY < 0 || realX >= fits.width || realY >= fits.height) {
                    buffer.put(0)
                    continue
                }

                val index = realY * fits.stride + realX * fits.pixelStride

                if (prevIndex == index) {
                    buffer.put(prevColor)
                } else if (fits.mono) {
                    val c = (fits.data[index] * 255f).toInt()
                    prevColor = 0xFF000000.toInt() or (c shl 16) or (c shl 8) or c
                    buffer.put(prevColor)
                } else {
                    val a = (fits.data[index] * 255f).toInt()
                    val b = (fits.data[index + 1] * 255f).toInt()
                    val c = (fits.data[index + 2] * 255f).toInt()
                    prevColor = 0xFF000000.toInt() or (a shl 16) or (b shl 8) or c
                    buffer.put(prevColor)
                }

                prevIndex = index
            }
        }

        buffer.flip()

        val pixelBuffer = PixelBuffer(areaWidth, areaHeight, buffer, PixelFormat.getIntArgbPreInstance())
        val writableImage = WritableImage(pixelBuffer)
        image.image = writableImage
    }

    @FXML
    private fun showImageStretcher() {
        imageStretcher.show()
    }
}
