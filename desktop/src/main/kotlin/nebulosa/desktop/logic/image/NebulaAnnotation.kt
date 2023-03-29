package nebulosa.desktop.logic.image

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import nebulosa.desktop.gui.control.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.stellarium.Nebula
import nebulosa.wcs.WCSTransform

class NebulaAnnotation(
    val calibration: Calibration,
    nebula: Nebula,
) : Drawable() {

    private val wcs = WCSTransform(calibration)
    private val data = nebula
        .searchAround(calibration.rightAscension, calibration.declination, calibration.radius)

    init {
        val width = calibration.crpix1 * 2.0
        val height = calibration.crpix2 * 2.0

        data.forEach {
            val (x, y) = wcs.worldToPixel(it.rightAscension, it.declination)

            // TODO: Ver comentário na classe Drawable.
            if (x < 0 || y < 0 || x >= width || y >= height) return@forEach

            val circle = Circle(x, y, 64.0)
            circle.fill = Color.TRANSPARENT
            circle.stroke = Color.GREEN
            circle.strokeWidth = 2.0
            add(circle)

            val text = Text(x, y, it.names.joinToString(" | "))
            text.stroke = Color.GREEN
            text.textAlignment = TextAlignment.CENTER
            add(text)
        }
    }

    override fun redraw(width: Double, height: Double) {}
}
