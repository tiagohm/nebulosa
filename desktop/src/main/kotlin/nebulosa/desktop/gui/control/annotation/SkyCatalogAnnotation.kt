package nebulosa.desktop.gui.control.annotation

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import nebulosa.desktop.gui.control.ShapePane
import nebulosa.desktop.withIO
import nebulosa.desktop.withMain
import nebulosa.math.Angle
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.DSO
import nebulosa.skycatalog.SkyCatalog
import nebulosa.skycatalog.SkyObject
import nebulosa.wcs.WCSTransform
import kotlin.math.max
import kotlin.math.min

class SkyCatalogAnnotation : ShapePane() {

    private val catalogs = HashSet<SkyCatalog<*>>(2)
    private val colors = HashMap<SkyCatalog<*>, Color>()

    fun add(
        catalog: SkyCatalog<*>,
        color: Color = Color.YELLOW,
    ) {
        if (catalogs.add(catalog)) {
            colors[catalog] = color
        }
    }

    fun remove(catalog: SkyCatalog<*>) {
        catalogs.remove(catalog)
        colors.remove(catalog)
    }

    suspend fun drawAround(calibration: Calibration) = withIO {
        val wcs = WCSTransform(calibration)

        val stars = ArrayList<Pair<Circle, Text>>(32)
        val width = calibration.crpix1 * 2.0
        val height = calibration.crpix2 * 2.0

        for (catalog in catalogs) {
            val color = colors[catalog] ?: Color.YELLOW

            stars.addAll(
                catalog
                    .searchAround(calibration.rightAscension, calibration.declination, calibration.radius)
                    .map { wcs.worldToPixel(it.rightAscension, it.declination).makeShapes(it, calibration, color) }
                    .filter { it.first.intersects(0.0, 0.0, width, height) })
        }

        withMain {
            children.removeAll { it is Circle || it is Text }
            stars.forEach { add(it.first); add(it.second) }
            redraw()
        }
    }

    override fun redraw(width: Double, height: Double) {}

    companion object {

        @JvmStatic
        private fun DoubleArray.makeShapes(star: SkyObject, calibration: Calibration, color: Color): Pair<Circle, Text> {
            val majorAxis = if (star is DSO) star.majorAxis else Angle.ZERO
            val majorAxisSize = max(14.0, min(majorAxis / calibration.scale, 256.0))

            val circle = Circle(this[0], this[1], 64.0)

            with(circle) {
                fill = Color.TRANSPARENT
                stroke = color
                strokeWidth = 1.0
                radius = majorAxisSize
            }

            val text = Text(this[0], this[1], star.names.joinToString(" | "))

            with(text) {
                fill = color
                stroke = color
                textAlignment = TextAlignment.CENTER
            }

            return circle to text
        }
    }
}
