package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import nebulosa.desktop.view.image.Drawable
import nebulosa.platesolving.Calibration
import nebulosa.stellarium.skycatalog.Nebula
import nebulosa.wcs.WCSTransform
import org.slf4j.LoggerFactory
import kotlin.math.max

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
        graphics.lineWidth = 3.0
        graphics.textAlign = TextAlignment.CENTER
        graphics.stroke = Color.GREEN
        graphics.fill = Color.GREEN

        val minSize = 25.0

        for (item in data) {
            val (x, y) = wcs.worldToPixel(item.ra, item.dec)

            val majorAxisSize = max(minSize, item.majorAxis / calibration.scale)
            // val minorAxisSize = max(minSize, min(maxSize, item.minorAxis / calibration.scale))

            graphics.strokeOval(x - majorAxisSize / 2, y - majorAxisSize / 2, majorAxisSize, majorAxisSize)
            graphics.fillText(item.names.joinToString(" | "), x, y - majorAxisSize / 2 - 8.0)
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(NebulaAnnotation::class.java)
    }
}
