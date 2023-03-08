package nebulosa.desktop.logic.image

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import nebulosa.desktop.logic.image.Annotation.Companion.distanceBetween
import nebulosa.desktop.view.image.Drawable
import nebulosa.io.resource
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.PairOfAngle
import nebulosa.platesolving.Calibration
import nebulosa.wcs.WCSTransform
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

data class MessierAnnotation(val calibration: Calibration) : Drawable {

    private data class Messier(
        val name: String,
        val coordinate: PairOfAngle,
        val magnitude: Double,
        val diameter: Double,
    )

    private val data: List<Messier>
    private val wcs = WCSTransform(calibration)

    init {
        load()

        val center = PairOfAngle(calibration.rightAscension, calibration.declination)

        data = DATA
            .filter { distanceBetween(it.coordinate, center).value < calibration.radius.value }

        LOG.info(
            "found {} Messier objects around coordinate. ra={}, dec={}, radius={}",
            data.size, center.first.hours, center.second.degrees, calibration.radius.degrees,
        )
    }

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = 0.5
        graphics.textAlign = TextAlignment.CENTER
        graphics.stroke = Color.BLUE
        graphics.fill = Color.BLUE

        val maxSize = min(width, height) / 2.0

        for (messier in data) {
            val (x, y) = wcs.worldToPixel(messier.coordinate.first, messier.coordinate.second)

            val size = max(NamedStarsAnnotation.STAR_SIZE, min(maxSize, messier.diameter / calibration.scale.arcmin))

            graphics.strokeOval(x - size / 2, y - size / 2, size, size)
            graphics.fillText(messier.name, x, y - size / 2 - 8.0)
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(MessierAnnotation::class.java)
        @JvmStatic private val DATA = ArrayList<Messier>(110)

        @JvmStatic
        private fun load() {
            if (DATA.isNotEmpty()) return

            val reader = resource("data/annotation/MESSIER.txt")!!.bufferedReader()

            for (line in reader.lines().skip(1)) {
                val parts = line.split("\t")
                val (code, ra, dec, dia, mag) = parts
                val name = if (parts.size >= 6) parts[5].trim() else ""

                DATA.add(
                    Messier(
                        if (name.isEmpty()) code.trim() else "%s (%s)".format(code.trim(), name),
                        PairOfAngle(ra.trim().toDouble().hours, dec.trim().toDouble().deg),
                        mag.trim().toDouble(),
                        dia.trim().toDouble(),
                    )
                )
            }
        }
    }
}
