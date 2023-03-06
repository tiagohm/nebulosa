package nebulosa.desktop.gui.image

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import nebulosa.desktop.gui.image.Annotation.Companion.distanceBetween
import nebulosa.desktop.view.image.Drawable
import nebulosa.io.resource
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.PairOfAngle
import nebulosa.platesolving.Calibration
import nebulosa.wcs.WCSTransform
import org.slf4j.LoggerFactory

data class NamedStarsAnnotation(val calibration: Calibration) : Drawable {

    private data class NamedStar(
        val name: String,
        val coordinate: PairOfAngle,
        val magnitude: Double,
    )

    private val data: List<NamedStar>
    private val wcs = WCSTransform(calibration)

    init {
        load()

        val center = PairOfAngle(calibration.rightAscension, calibration.declination)

        data = DATA
            .filter { distanceBetween(it.coordinate, center).value < calibration.radius.value }

        LOG.info("found {} named stars around coordinate RA={} DEC={}", data.size, center.first.hours, center.second.degrees)
    }

    override fun draw(width: Double, height: Double, graphics: GraphicsContext) {
        graphics.lineWidth = 0.5
        graphics.textAlign = TextAlignment.LEFT
        graphics.stroke = Color.YELLOW
        graphics.fill = Color.YELLOW

        for (star in data) {
            val (x, y) = wcs.worldToPixel(star.coordinate.first, star.coordinate.second)

            graphics.strokeOval(x - STAR_SIZE / 2, y - STAR_SIZE / 2, STAR_SIZE, STAR_SIZE)
            graphics.fillText(star.name, x + STAR_SIZE / 2, y - STAR_SIZE / 2)
        }
    }

    companion object {

        const val STAR_SIZE = 28.0

        @JvmStatic private val LOG = LoggerFactory.getLogger(NamedStarsAnnotation::class.java)
        @JvmStatic private val DATA = ArrayList<NamedStar>(3671)

        @JvmStatic
        private fun load() {
            if (DATA.isNotEmpty()) return

            val reader = resource("data/annotation/NAMED_STARS.txt")!!.bufferedReader()

            for (line in reader.lines().skip(1)) {
                val parts = line.split("\t")
                val (bhd, ra, dec, _, mag) = parts
                val name = if (parts.size >= 8) parts[7].trim() else ""

                DATA.add(
                    NamedStar(
                        if (name.isEmpty()) bhd.trim() else "%s (%s)".format(name, bhd.trim()),
                        PairOfAngle(ra.trim().toDouble().hours, dec.trim().toDouble().deg),
                        mag.trim().toDouble(),
                    )
                )
            }
        }
    }
}
