package nebulosa.desktop.logic.image

import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import nebulosa.desktop.gui.control.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.hyg.HygDatabase
import nebulosa.wcs.WCSTransform

data class StarsAnnotation(
    val calibration: Calibration,
    val hygDatabase: HygDatabase,
) : Drawable() {

    private val wcs = WCSTransform(calibration)
    private var data = hygDatabase
        .searchAround(calibration.rightAscension, calibration.declination, calibration.radius)

    init {
        val width = calibration.crpix1 * 2.0
        val height = calibration.crpix2 * 2.0

        data.forEach {
            val (x, y) = wcs.worldToPixel(it.rightAscension, it.declination)

            // TODO: Ver comentário na classe Drawable.
            if (x < 0 || y < 0 || x >= width || y >= height) return@forEach

            val circle = Circle(x, y, 28.0)
            circle.fill = Color.TRANSPARENT
            circle.stroke = Color.YELLOW
            circle.strokeWidth = 2.0
            add(circle)

            val text = Text(x, y, it.names.joinToString(" | "))
            text.stroke = Color.YELLOW
            text.textAlignment = TextAlignment.CENTER
            add(text)
        }
    }

    override fun redraw(width: Double, height: Double) {}
}
