import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.fits.Fits
import nebulosa.fits.Header
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.wcs.WCS
import kotlin.random.Random

// https://www.atnf.csiro.au/people/mcalabre/WCS/example_data.html

@EnabledIf(NonGitHubOnlyCondition::class)
class LibWCSTest : StringSpec() {

    init {
        for (projection in PROJECTIONS) {
            projection {
                pixToSky(projection, 192, 192)
            }
        }

        "TAN-SIP" {
            pixToSky("TAN-SIP", 1280, 720)
        }
    }

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

    private fun readHeaderFromFits(name: String): Header {
        return Fits("src/test/resources/$name.fits").use { it.readHdu()!!.header }
    }

    companion object {

        @JvmStatic private val PROJECTIONS = arrayOf(
            "AIR", "AIT", "ARC", "AZP", "CAR", "CEA",
            "CSC", "CYP", "HPX", "MER", "MOL",
            "NCP", "PAR", "PCO", "QSC", "SFL", "SIN", "STG", "SZP",
            "TAN", "TSC", "ZEA",
            "BON", "COD", "COE", "COO", "COP", "ZPN" // FAILED
        )
    }
}
