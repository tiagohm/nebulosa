package nebulosa.desktop.gui.control

import javafx.event.EventHandler
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import nebulosa.desktop.helper.withIO
import nebulosa.desktop.helper.withMain
import nebulosa.desktop.service.SkyObjectService
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.AxisSize
import nebulosa.skycatalog.SkyObject
import nebulosa.wcs.WCSTransform
import kotlin.math.max
import kotlin.math.min

typealias AnnotationFilter = (String, SkyObjectService.Filter) -> List<SkyObject>

class Annotation : ShapePane() {

    interface EventListener {

        fun onStarClicked(star: SkyObject)
    }

    private val catalogs = HashSet<AnnotationFilter>(2)
    private val colors = HashMap<AnnotationFilter, Color>(2)
    private val eventListeners = HashSet<EventListener>(1)

    fun add(
        catalog: AnnotationFilter,
        color: Color = Color.YELLOW,
    ) {
        if (catalogs.add(catalog)) {
            colors[catalog] = color
        }
    }

    fun registerEventListener(listener: EventListener) {
        eventListeners.add(listener)
    }

    fun unregisterEventListener(listener: EventListener) {
        eventListeners.remove(listener)
    }

    fun remove(catalog: AnnotationFilter) {
        catalogs.remove(catalog)
        colors.remove(catalog)
    }

    suspend fun drawAround(calibration: Calibration) = withIO {
        val wcs = WCSTransform(calibration)

        val stars = ArrayList<Pair<Circle, Text>>(32)
        val width = calibration.crpix1 * 2.0
        val height = calibration.crpix2 * 2.0

        LOG.info(
            "annotation around star. ra={}, dec={}, radius={}",
            calibration.rightAscension.degrees, calibration.declination.degrees, calibration.radius.degrees
        )

        val filter = SkyObjectService.Filter(calibration.rightAscension, calibration.declination, calibration.radius)

        for (catalog in catalogs) {
            val color = colors[catalog] ?: Color.YELLOW

            stars.addAll(
                catalog
                    .invoke("", filter)
                    .map { wcs.worldToPixel(it.rightAscension, it.declination).makeShapes(this@Annotation, it, calibration, color) }
                    .filter { it.first.intersects(0.0, 0.0, width, height) })
        }

        stars.sortByDescending { it.first.radius }

        withMain {
            children.removeAll { it is Circle || it is Text }
            stars.forEach { add(it.first); add(it.second) }
            redraw()
        }
    }

    override fun redraw(width: Double, height: Double) {}

    companion object {

        @JvmStatic private val LOG = loggerFor<Annotation>()

        @JvmStatic
        private fun DoubleArray.makeShapes(annotation: Annotation, star: SkyObject, calibration: Calibration, color: Color): Pair<Circle, Text> {
            val majorAxis = if (star is AxisSize) star.majorAxis else Angle.ZERO
            val majorAxisSize = max(14.0, min(majorAxis / calibration.scale, 380.0))

            val circle = Circle(this[0], this[1], 64.0)

            val starClicked = EventHandler<MouseEvent> { event ->
                if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                    event.consume()
                    annotation.eventListeners.forEach { it.onStarClicked(star) }
                }
            }

            with(circle) {
                fill = Color.TRANSPARENT
                stroke = color
                strokeWidth = 1.0
                radius = majorAxisSize
                addEventHandler(MouseEvent.MOUSE_CLICKED, starClicked)
            }

            val text = Text(this[0], this[1], star.names)

            with(text) {
                fill = color
                stroke = color
                textAlignment = TextAlignment.CENTER
                addEventHandler(MouseEvent.MOUSE_CLICKED, starClicked)
            }

            return circle to text
        }
    }
}
