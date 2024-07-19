import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.format.ReadableHeader
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.test.NonGitHubOnly
import nebulosa.wcs.WCS
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.random.Random

// https://www.atnf.csiro.au/people/mcalabre/WCS/example_data.html

@NonGitHubOnly
class LibWCSTest {

    @Test
    fun air() {
        pixToSky("AIR", 192, 192)
    }

    @Test
    fun ait() {
        pixToSky("AIT", 192, 192)
    }

    @Test
    fun arc() {
        pixToSky("ARC", 192, 192)
    }

    @Test
    fun azp() {
        pixToSky("AZP", 192, 192)
    }

    @Test
    fun car() {
        pixToSky("CAR", 192, 192)
    }

    @Test
    fun cea() {
        pixToSky("CEA", 192, 192)
    }

    @Test
    fun csc() {
        pixToSky("CSC", 192, 192)
    }

    @Test
    fun cyp() {
        pixToSky("CYP", 192, 192)
    }

    @Test
    fun hpx() {
        pixToSky("HPX", 192, 192)
    }

    @Test
    fun mer() {
        pixToSky("MER", 192, 192)
    }

    @Test
    fun mol() {
        pixToSky("MOL", 192, 192)
    }

    @Test
    fun ncp() {
        pixToSky("NCP", 192, 192)
    }

    @Test
    fun par() {
        pixToSky("PAR", 192, 192)
    }

    @Test
    fun pco() {
        pixToSky("PCO", 192, 192)
    }

    @Test
    fun qsc() {
        pixToSky("QSC", 192, 192)
    }

    @Test
    fun sfl() {
        pixToSky("SFL", 192, 192)
    }

    @Test
    fun sin() {
        pixToSky("SIN", 192, 192)
    }

    @Test
    fun stg() {
        pixToSky("STG", 192, 192)
    }

    @Test
    fun szp() {
        pixToSky("SZP", 192, 192)
    }

    @Test
    fun tan() {
        pixToSky("TAN", 192, 192)
    }

    @Test
    fun tsc() {
        pixToSky("TSC", 192, 192)
    }

    @Test
    fun zea() {
        pixToSky("ZEA", 192, 192)
    }

    @Test
    fun tanSip() {
        pixToSky("TAN-SIP", 1280, 720)
    }

    // "BON", "COD", "COE", "COO", "COP", "ZPN" // FAILED

    companion object {

        @JvmStatic
        private fun pixToSky(projectionName: String, width: Int, height: Int) {
            val data = Array(2048) { intArrayOf(Random.nextInt(width), Random.nextInt(height)) }

            WCS(readHeaderFromFits(projectionName)).use {
                val topLeft = it.pixToSky(0.0, 0.0)
                val topRight = it.pixToSky(width.toDouble(), 0.0)
                val bottomLeft = it.pixToSky(0.0, height.toDouble())
                val bottomRight = it.pixToSky(width.toDouble(), height.toDouble())
                val center = it.pixToSky(width / 2.0, height / 2.0)

                println("top left: ${topLeft.rightAscension.formatHMS()} ${topLeft.declination.formatSignedDMS()}")
                println("top right: ${topRight.rightAscension.formatHMS()} ${topRight.declination.formatSignedDMS()}")
                println("bottom left: ${bottomLeft.rightAscension.formatHMS()} ${bottomLeft.declination.formatSignedDMS()}")
                println("bottom right: ${bottomRight.rightAscension.formatHMS()} ${bottomRight.declination.formatSignedDMS()}")
                println("center: ${center.rightAscension.formatHMS()} ${center.declination.formatSignedDMS()}")

                for ((x0, y0) in data) {
                    val (rightAscension, declination) = it.pixToSky(x0.toDouble(), y0.toDouble())
                    val (x1, y1) = it.skyToPix(rightAscension, declination)
                    x1 shouldBe (x0.toDouble() plusOrMinus 1.0)
                    y1 shouldBe (y0.toDouble() plusOrMinus 1.0)
                }
            }
        }

        @JvmStatic
        private fun readHeaderFromFits(name: String): ReadableHeader {
            return Path.of("src/test/resources/$name.fits").fits().use { it.first!!.header }
        }
    }
}
