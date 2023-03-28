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

    fun initialize() {
        data.forEach {
            val xy = wcs.worldToPixel(it.rightAscension, it.declination)
            val circle = Circle(xy[0], xy[1], 28.0)
            circle.fill = Color.TRANSPARENT
            circle.stroke = Color.YELLOW
            circle.strokeWidth = 2.0
            add(circle)

            val text = Text(xy[0], xy[1], it.names.joinToString(" | "))
            text.stroke = Color.YELLOW
            text.textAlignment = TextAlignment.CENTER
            add(text)
        }
    }

    override fun redraw() {}
}
