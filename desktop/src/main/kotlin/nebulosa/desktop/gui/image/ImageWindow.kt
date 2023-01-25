package nebulosa.desktop.gui.image

import javafx.fxml.FXML
import javafx.geometry.Bounds
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
import javafx.stage.Screen
import nebulosa.desktop.gui.AbstractWindow
import nebulosa.desktop.logic.image.ImageManager
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.ProtectionMethod
import nebulosa.indi.device.cameras.Camera
import nom.tam.fits.Header
import java.io.File
import java.nio.IntBuffer

class ImageWindow(@JvmField val camera: Camera? = null) : AbstractWindow() {

    override val resourceName = "Image"

    @FXML private lateinit var imageCanvas: Canvas
    @FXML private lateinit var menu: ContextMenu
    @FXML private lateinit var scnrMenuItem: MenuItem
    @FXML private lateinit var fitsHeaderMenuItem: MenuItem

    @Volatile private var buffer = IntArray(0)

    private val screenBounds = Screen.getPrimary().bounds
    private val imageManager = ImageManager(this)
    @Volatile private var imageStretcherWindow: ImageStretcherWindow? = null
    @Volatile private var fitsHeaderWindow: FitsHeaderWindow? = null
    @Volatile private var scnrWindow: SCNRWindow? = null

    init {
        title = "Image"

        val area = (screenBounds.width * screenBounds.height).toInt()
        buffer = IntArray(area)
    }

    override fun onCreate() {
        imageCanvas.addEventFilter(ScrollEvent.SCROLL) {
            if (it.deltaX != 0.0 || it.deltaY != 0.0) {
                imageManager.zoomWithWheel(it)
                it.consume()
            }
        }

        imageCanvas.addEventFilter(MouseEvent.MOUSE_DRAGGED) {
            if (it.button == MouseButton.PRIMARY) {
                imageCanvas.cursor = Cursor.MOVE
                imageManager.drag(it)
                it.consume()
            }
        }

        imageCanvas.addEventFilter(MouseEvent.MOUSE_RELEASED) {
            imageCanvas.cursor = Cursor.DEFAULT
            imageManager.dragStop(it)
        }

        imageCanvas.parent.addEventFilter(MouseEvent.MOUSE_CLICKED) {
            if (it.button == MouseButton.PRIMARY && it.clickCount == 2) {
                imageManager.resetZoom()

                if (isMaximized) {
                    isMaximized = false
                    imageManager.adjustSceneSizeToFitImage(true)
                } else {
                    imageManager.adjustSceneSizeToFitImage(false)
                }

                imageManager.draw()
            } else if (it.button == MouseButton.PRIMARY) {
                menu.hide()
                it.consume()
            }
        }

        imageCanvas.parent.setOnContextMenuRequested {
            menu.show(imageCanvas.parent, it.screenX, it.screenY)
        }
    }

    override fun onStart() {
        imageManager.loadPreferences()
    }

    override fun onStop() {
        buffer = IntArray(0)

        imageStretcherWindow?.close()
        scnrWindow?.close()
        fitsHeaderWindow?.close()

        imageManager.close()

        fitsHeaderWindow?.close()
        fitsHeaderWindow = null

        scnrWindow?.close()
        scnrWindow = null

        System.gc()
    }

    val fits
        get() = imageManager.transformedFits ?: imageManager.fits

    val shadow
        get() = imageManager.shadow

    val highlight
        get() = imageManager.highlight

    val midtone
        get() = imageManager.midtone

    val mirrorHorizontal
        get() = imageManager.mirrorHorizontal

    val mirrorVertical
        get() = imageManager.mirrorVertical

    val invert
        get() = imageManager.invert

    val scnrEnabled
        get() = imageManager.scnrEnabled

    val scnrChannel
        get() = imageManager.scnrChannel

    val scnrProtectionMode
        get() = imageManager.scnrProtectionMode

    val scnrAmount
        get() = imageManager.scnrAmount

    var imageWidth
        get() = imageCanvas.width
        set(value) {
            imageCanvas.width = value
        }

    var imageHeight
        get() = imageCanvas.height
        set(value) {
            imageCanvas.height = value
        }

    var hasScnr
        get() = !scnrMenuItem.isDisable
        set(value) {
            scnrMenuItem.isDisable = !value
        }

    var hasFitsHeader
        get() = !fitsHeaderMenuItem.isDisable
        set(value) {
            fitsHeaderMenuItem.isDisable = !value
        }

    val imageBounds: Bounds
        get() = imageCanvas.parent.boundsInLocal

    @FXML
    private fun openImageStretcher() {
        imageStretcherWindow = imageStretcherWindow ?: ImageStretcherWindow(this)
        imageStretcherWindow!!.open(bringToFront = true)
    }

    @FXML
    private fun openSCNR() {
        scnrWindow = scnrWindow ?: SCNRWindow(this)
        scnrWindow!!.open(bringToFront = true)
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

    fun open(file: File) {
        imageManager.open(file)
    }

    fun openFitsHeader(header: Header) {
        fitsHeaderWindow = fitsHeaderWindow ?: FitsHeaderWindow()
        fitsHeaderWindow!!.load(header)
        fitsHeaderWindow!!.open(bringToFront = true)
    }

    fun draw(
        fits: Image,
        width: Int, height: Int,
        startX: Int, startY: Int,
        factor: Float,
    ) {
        var prevIndex = -1
        var prevColor = 0
        var idx = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
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
        val pixelBuffer = PixelBuffer(width, height, intBuffer, PixelFormat.getIntArgbPreInstance())
        val writableImage = WritableImage(pixelBuffer)

        val g = imageCanvas.graphicsContext2D
        g.clearRect(0.0, 0.0, imageCanvas.width, imageCanvas.height)
        g.drawImage(writableImage, 0.0, 0.0)
    }

    fun drawHistogram() {
        imageStretcherWindow?.drawHistogram()
    }

    fun applySCNR(
        enabled: Boolean, channel: ImageChannel,
        protectionMethod: ProtectionMethod, amount: Double,
    ) {
        imageManager.transformImage(
            scnrEnabled = enabled, scnrChannel = channel,
            scnrProtectionMode = protectionMethod,
            scnrAmount = amount.toFloat(),
        )
    }

    fun applySTF(shadow: Float, highlight: Float, midtone: Float) {
        imageManager.transformImage(shadow = shadow, highlight = highlight, midtone = midtone)
    }

    companion object {

        @JvmStatic private val windows = hashSetOf<ImageWindow>()

        @JvmStatic
        fun open(file: File, camera: Camera? = null): ImageWindow {
            val window = windows
                .firstOrNull { if (camera == null) it.camera == null && !it.isShowing else it.camera === camera }
                ?: ImageWindow(camera)

            windows.add(window)

            window.show()
            window.open(file)

            return window
        }
    }
}
