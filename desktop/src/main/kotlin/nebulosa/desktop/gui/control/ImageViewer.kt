package nebulosa.desktop.gui.control

import javafx.geometry.Point2D
import javafx.scene.CacheHint
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import nebulosa.desktop.view.image.Drawable
import net.kurobako.gesturefx.GesturePane
import java.awt.event.MouseListener
import java.util.*
import kotlin.math.exp

class ImageViewer private constructor(private val drawables: LinkedList<Drawable>) :
    GesturePane(null as Node?), Deque<Drawable> by drawables {

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
    private var image: Image? = null

    constructor() : this(LinkedList())

    init {
        canvas.isCache = false
        canvas.cacheHint = CacheHint.SPEED

        content = canvas

        addEventFilter(ScrollEvent.SCROLL) {
            if (it.deltaX != 0.0 || it.deltaY != 0.0) {
                val delta = if (it.deltaY == 0.0 && it.deltaX != 0.0) it.deltaX else it.deltaY
                val wheel = if (delta < 0) -1 else 1

                val newScale = currentScale * exp((wheel * 0.25) / 3)

                val pivotOnTarget = targetPointAt(Point2D(it.x, it.y))
                    .orElse(targetPointAtViewportCentre())

                zoomTo(newScale, pivotOnTarget)

                it.consume()
            }
        }

        addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
            val target = targetPointAt(Point2D(event.x, event.y)).orElse(targetPointAtViewportCentre())
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
        redraw()
    }

    fun redraw() {
        val image = image ?: return

        canvas.width = image.width
        canvas.height = image.height

        with(canvas.graphicsContext2D) {
            isImageSmoothing = false

            clearRect(0.0, 0.0, canvas.width, canvas.height)
            drawImage(image, 0.0, 0.0, canvas.width, canvas.height)

            drawables.forEach { it.draw(canvas.width, canvas.height, this) }
        }
    }

    fun resetZoom() {
        zoomTo(0.0, targetPointAtViewportCentre())
    }
}
