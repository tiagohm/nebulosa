package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import nebulosa.desktop.view.image.Drawable
import nebulosa.math.PairOfAngle
import nebulosa.platesolving.Calibration
import nebulosa.skycatalog.brightstars.BrightStars
import nebulosa.wcs.WCSTransform
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

data class BrightStarsAnnotation(val calibration: Calibration) : Drawable {

    private val wcs = WCSTransform(calibration)
    private val data = BrightStars
        .searchAround(calibration.rightAscension, calibration.declination, calibration.radius)

    init {
        val center = PairOfAngle(calibration.rightAscension, calibration.declination)

        LOG.info(
            "found {} named stars around coordinate. ra={}, dec={}, radius={}",
            data.size, center.first.hours, center.second.degrees, calibration.radius.degrees,
        )
    }

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = 5.0
        graphics.stroke = Color.YELLOW
        graphics.fill = Color.YELLOW
        graphics.font = DEFAULT_FONT

        for (item in data) {
            val (x, y) = wcs.worldToPixel(item.rightAscension, item.declination)

            graphics.strokeOval(x - STAR_SIZE / 2, y - STAR_SIZE / 2, STAR_SIZE, STAR_SIZE)

            val textX = max(18.0, min(x, width - 18.0))
            val textY = max(18.0, min(y - STAR_SIZE / 2 - 18.0, height))
            val text = item.names.joinToString(" | ")

            graphics.textAlign = if (textX > width * 0.9) TextAlignment.RIGHT
            else if (textX > width * 0.1) TextAlignment.CENTER
            else TextAlignment.LEFT

            graphics.fillText(text, textX, textY)
        }
    }

    companion object {

        const val STAR_SIZE = 28.0

        @JvmStatic private val LOG = LoggerFactory.getLogger(BrightStarsAnnotation::class.java)
        @JvmStatic private val DEFAULT_FONT = Font.font(22.0)
    }
}
