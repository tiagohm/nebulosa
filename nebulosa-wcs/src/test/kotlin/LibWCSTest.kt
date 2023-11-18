import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestScope
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.fits.Fits
import nebulosa.fits.Header
import nebulosa.math.AngleFormatter
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.test.NonGitHubOnlyCondition
import nebulosa.wcs.WCSTransform
import kotlin.random.Random

// https://www.atnf.csiro.au/people/mcalabre/WCS/example_data.html

@EnabledIf(NonGitHubOnlyCondition::class)
class LibWCSTest : StringSpec() {

    private val pixToSky = ArrayList<IntArray>(192 * 8)
    private val skyToPix = ArrayList<DoubleArray>(192 * 8)

    init {
        repeat(192 * 8) {
            val x = Random.nextInt(192)
            val y = Random.nextInt(192)
            val ra = Random.nextDouble(18.0, 19.5).hours
            val dec = Random.nextDouble(59.0, 72.0).unaryMinus().deg
            pixToSky.add(intArrayOf(x, y))
            skyToPix.add(doubleArrayOf(ra, dec))
        }

        for (projection in PROJECTIONS) {
            "pixToSky:$projection" {
                pixToSky()
            }
            "skyToPix:$projection" {
                skyToPix()
            }
        }
    }

    private fun TestScope.pixToSky() {
        val testName = testCase.name.testName.split(":").last()
        val keywords = readHeaderFromFits(testName)
        val transform = WCSTransform(keywords)

        for ((x0, y0) in pixToSky) {
            val (rightAscension, declination) = transform.pixToSky(x0.toDouble(), y0.toDouble())
            val (x1, y1) = transform.skyToPix(rightAscension, declination)
            x1 shouldBe (x0.toDouble() plusOrMinus 1.0)
            y1 shouldBe (y0.toDouble() plusOrMinus 1.0)
        }

        transform.close()
    }

    private fun TestScope.skyToPix() {
        val testName = testCase.name.testName.split(":").last()
        val keywords = readHeaderFromFits(testName)
        val transform = WCSTransform(keywords)

        for ((rightAscension0, declination0) in skyToPix) {
            val (x, y) = transform.skyToPix(rightAscension0, declination0)
            val (rightAscension1, declination1) = transform.pixToSky(x, y)
            rightAscension1 shouldBe (rightAscension0 plusOrMinus EPSILON)
            declination1 shouldBe (declination0 plusOrMinus EPSILON)
        }

        transform.close()
    }

    private fun readHeaderFromFits(name: String): Header {
        return Fits("src/test/resources/$name.fits").readHdu()!!.header
    }

    companion object {

        const val EPSILON = 1 / 3600000.0

        @JvmStatic private val PROJECTIONS = arrayOf(
            "AIR", "AIT", "ARC", "AZP", "BON", "CAR", "CEA",
            "CSC", "CYP", "MER", "MOL",
            "NCP", "PAR", "PCO", "QSC", "SFL", "SIN", "STG", "SZP",
            "TAN", "TSC", "ZEA", "ZPN",
            // FAILED: "COD", "COE", "COO", "COP", "HPX"
        )

        @JvmStatic private val RA_FORMAT = AngleFormatter.Builder()
            .hours()
            .separators(":")
            .secondsDecimalPlaces(2)
            .build()

        @JvmStatic private val DEC_FORMAT = AngleFormatter.Builder()
            .separators(":")
            .secondsDecimalPlaces(2)
            .build()
    }
}
