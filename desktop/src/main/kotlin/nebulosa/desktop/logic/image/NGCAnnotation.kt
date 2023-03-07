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

data class NGCAnnotation(val calibration: Calibration) : Drawable {

    private data class NGC(
        val name: String,
        val coordinate: PairOfAngle,
        val magnitude: Double,
        val diameter: Double,
    )

    private val data: List<NGC>
    private val wcs = WCSTransform(calibration)

    init {
        load()

        val center = PairOfAngle(calibration.rightAscension, calibration.declination)

        data = DATA
            .filter { distanceBetween(it.coordinate, center).value < calibration.radius.value }

        LOG.info("found {} NGC objects around coordinate RA={} DEC={}", data.size, center.first.hours, center.second.degrees)
    }

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = 0.5
        graphics.textAlign = TextAlignment.CENTER
        graphics.stroke = Color.GREEN
        graphics.fill = Color.GREEN

        val maxSize = min(width, height) / 2.0

        for (ngc in data) {
            val (x, y) = wcs.worldToPixel(ngc.coordinate.first, ngc.coordinate.second)

            val size = max(NamedStarsAnnotation.STAR_SIZE, min(maxSize, ngc.diameter / calibration.scale.arcmin))

            graphics.strokeOval(x - size / 2, y - size / 2, size, size)
            graphics.fillText(ngc.name, x, y + size / 2 + 16.0)
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(NGCAnnotation::class.java)
        @JvmStatic private val DATA = ArrayList<NGC>(9933)

        @JvmStatic
        private fun load() {
            if (DATA.isNotEmpty()) return

            val reader = resource("data/annotation/NGC.txt")!!.bufferedReader()

            for (line in reader.lines().skip(1)) {
                val parts = line.split("\t")
                val (code, ra, dec, dia, mag) = parts
                val name = if (parts.size >= 6) parts[5].trim() else ""

                DATA.add(
                    NGC(
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
