package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import nebulosa.desktop.view.image.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.stellarium.skycatalog.Nebula
import nebulosa.wcs.WCSTransform
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

class NebulaAnnotation(
    val calibration: Calibration,
    nebula: Nebula,
) : Drawable {

    private val wcs = WCSTransform(calibration)
    private val data = nebula
        .searchAround(calibration.rightAscension, calibration.declination, calibration.radius)

    init {
        LOG.info(
            "found {} DSO objects around coordinates. ra={}, dec={}",
            data.size, calibration.rightAscension.hours, calibration.declination.hours
        )
    }

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = 5.0
        graphics.stroke = Color.GREEN
        graphics.fill = Color.GREEN
        graphics.font = DEFAULT_FONT

        val minSize = 25.0

        for (item in data) {
            val (x, y) = wcs.worldToPixel(item.ra, item.dec)

            if (x in 0.0..width && y in 0.0..height) {
                val majorAxisSize = max(minSize, item.majorAxis / calibration.scale)
                // val minorAxisSize = max(minSize, min(maxSize, item.minorAxis / calibration.scale))

                graphics.strokeOval(x - majorAxisSize / 2, y - majorAxisSize / 2, majorAxisSize, majorAxisSize)

                val textX = max(18.0, min(x, width - 18.0))
                val textY = max(18.0, min(y - majorAxisSize / 2 - 18.0, height))
                val text = item.names.joinToString(" | ")

                graphics.textAlign = if (textX > width * 0.9) TextAlignment.RIGHT
                else if (textX > width * 0.1) TextAlignment.CENTER
                else TextAlignment.LEFT

                graphics.fillText(text, textX, textY)
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(NebulaAnnotation::class.java)
        @JvmStatic private val DEFAULT_FONT = Font.font(22.0)
    }
}
