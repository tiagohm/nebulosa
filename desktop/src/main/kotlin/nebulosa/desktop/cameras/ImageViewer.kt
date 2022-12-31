package nebulosa.desktop.cameras

import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import nebulosa.desktop.internal.Window
import nebulosa.imaging.FitsImage
import nebulosa.indi.devices.cameras.Camera
import nom.tam.fits.Fits
import java.io.File
import java.nio.IntBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

class ImageViewer(val camera: Camera? = null) : Window("ImageViewer") {

    @FXML private lateinit var image: ImageView

    @Volatile private var fits: FitsImage? = null

    @Volatile private var buffer = IntBuffer.allocate(1)
    @Volatile private var bufferSize = 1
    @Volatile private var scale = 1.0
    @Volatile private var startX = 0
    @Volatile private var startY = 0
    @Volatile private var dragging = false
    @Volatile private var dragStartX = 0.0
    @Volatile private var dragStartY = 0.0
    @Volatile private var lastDrawTime = 0L

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
        zoomToPoint(min(max(newScale, 1.0), 10.0), event.x, event.y)
    }

    private fun zoomToPoint(
        newScale: Double,
        pointX: Double, pointY: Double,
    ) {
        val bounds = image.parent.boundsInLocal

        val x = (pointX / bounds.width) * (bounds.width * newScale)
        val y = (pointY / bounds.height) * (bounds.height * newScale)

        val toX = (x / newScale - x / scale + x * newScale) / newScale
        val toY = (y / newScale - y / scale + y * newScale) / newScale

        // val x = pointX + startX
        // val y = pointY + startY

        scale = newScale
        startX -= (toX - x).toInt()
        startY -= (toY - y).toInt()

        println("$x $y $toX $toY $startX $startY")

        draw()
    }

    @Synchronized
    fun open(file: File) {
        setTitleFromCameraAndFile(file)

        show()

        val fits = FitsImage(Fits(file))
        fits.read()
        this.fits = fits

        draw()
    }

    private fun draw() {
        if (System.currentTimeMillis() - lastDrawTime < 100) return

        lastDrawTime = System.currentTimeMillis()

        val fits = fits ?: return

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
}
