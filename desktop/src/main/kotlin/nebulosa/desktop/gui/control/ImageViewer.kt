package nebulosa.desktop.gui.control

import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.CacheHint
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.StackPane
import nebulosa.desktop.helper.withMain
import net.kurobako.gesturefx.GesturePane
import kotlin.math.exp
import kotlin.math.max

class ImageViewer : GesturePane(null as Node?) {

    fun interface MouseListener {

        fun onMouseClicked(
            button: MouseButton,
            clickCount: Int,
            isControlDown: Boolean, isShiftDown: Boolean, isAltDown: Boolean,
            mouseX: Double, mouseY: Double,
            imageX: Double, imageY: Double,
        )
    }

    private val canvas = Canvas()
    private val mouseListeners = HashSet<MouseListener>(1)
    private val root = StackPane()
    private var image: Image? = null

    init {
        canvas.isCache = false
        canvas.cacheHint = CacheHint.SPEED
        canvas.graphicsContext2D.isImageSmoothing = false

        root.alignment = Pos.TOP_LEFT
        root.children.add(canvas)

        content = root

        addEventFilter(ScrollEvent.SCROLL) {
            if (it.deltaX != 0.0 || it.deltaY != 0.0) {
                val delta = if (it.deltaY == 0.0 && it.deltaX != 0.0) it.deltaX else it.deltaY
                val wheel = if (delta < 0) -1 else 1

                val newScale = currentScale * exp((wheel * 0.3) / 3)

                val pivotOnTarget = targetPointAt(Point2D(it.x, it.y))
                    .orElse(targetPointAtViewportCentre())

                zoomTo(newScale, pivotOnTarget)

                it.consume()
            }
        }

        addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            val target = targetPointAt(Point2D(event.x, event.y))
                .orElse(targetPointAtViewportCentre())

            mouseListeners.forEach {
                it.onMouseClicked(
                    event.button, event.clickCount,
                    event.isControlDown, event.isShiftDown, event.isAltDown,
                    event.x, event.y, target.x, target.y,
                )
            }
        }

        addEventFilter(MouseEvent.MOUSE_DRAGGED) {
            if (it.button == MouseButton.PRIMARY) {
                cursor = Cursor.MOVE
            }
        }

        addEventFilter(MouseEvent.MOUSE_RELEASED) {
            cursor = Cursor.DEFAULT
        }
    }

    fun registerMouseListener(listener: MouseListener) {
        mouseListeners.add(listener)
    }

    fun unregisterMouseListener(listener: MouseListener) {
        mouseListeners.remove(listener)
    }

    fun load(image: Image) {
        this.image = image
        root.prefWidth = image.width
        root.prefHeight = image.height
        redraw()
    }

    fun redraw() {
        val image = image ?: return

        // TODO: Escalar melhor isso aqui!
        canvas.width = max(image.width, width)
        canvas.height = max(image.height, height)

        with(canvas.graphicsContext2D) {
            clearRect(0.0, 0.0, canvas.width, canvas.height)
            drawImage(image, 0.0, 0.0, canvas.width, canvas.height)
        }
    }

    suspend fun resetZoom() = withMain {
        zoomTo(0.0, targetPointAtViewportCentre())
    }

    fun addFirst(shape: Node) {
        if (shape !in root.children) {
            root.children.add(1, shape)
        }
    }

    fun addLast(shape: Node) {
        if (shape !in root.children) {
            root.children.add(shape)
        }
    }

    fun remove(shape: Node) {
        root.children.remove(shape)
    }

    fun removeFirst(): Node? {
        return if (root.children.size > 1) {
            root.children.removeAt(1)
        } else {
            null
        }
    }

    fun removeLast(): Node? {
        return if (root.children.size > 1) {
            root.children.removeLast()
        } else {
            null
        }
    }
}
