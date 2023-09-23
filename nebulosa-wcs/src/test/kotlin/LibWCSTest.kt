import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestScope
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.io.resource
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.AngleFormatter
import nebulosa.wcs.WCSTransform
import nom.tam.fits.Fits
import nom.tam.fits.Header
import java.nio.file.Path
import kotlin.random.Random

// https://www.atnf.csiro.au/people/mcalabre/WCS/example_data.html

class LibWCSTest : StringSpec() {

    private val pixToSky = ArrayList<IntArray>(192 * 8)
    private val skyToPix = ArrayList<Array<String>>(192 * 8)

    init {
        repeat(192 * 8) {
            val x = Random.nextInt(192)
            val y = Random.nextInt(192)
            val ra = Random.nextDouble(18.0, 19.5).hours
            val dec = Random.nextDouble(59.0, 72.0).unaryMinus().deg
            pixToSky.add(intArrayOf(x, y))
            skyToPix.add(arrayOf(ra.format(RA_FORMAT), dec.format(DEC_FORMAT)))
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
        val testName = testCase.name.testName.split(":")[1]
        val keywords = readHeaderFromFits(testName)
        val transform = WCSTransform(keywords)

        val fitsPath = Path.of("src/test/resources/$testName.fits")
        val pixels = Array(pixToSky.size * 2) { pixToSky[it / 2][it % 2].toString() }
        val process = ProcessBuilder("xy2sky", "$fitsPath", *pixels).start()

        for (line in process.inputStream.bufferedReader().lines()) {
            val parts = line.replace("->", "").split(WHITESPACE_REGEX)
            val rightAscension0 = parts[0].hours
            val declination0 = parts[1].deg
            val x = parts[3].toDouble()
            val y = parts[4].toDouble()

            val (rightAscension1, declination1) = transform.pixToSky(x, y)
            rightAscension0.value shouldBe (rightAscension1.value plusOrMinus EPSILON)
            declination0.value shouldBe (declination1.value plusOrMinus EPSILON)
        }

        transform.close()
    }

    private fun TestScope.skyToPix() {
        val testName = testCase.name.testName.split(":")[1]
        val keywords = readHeaderFromFits(testName)
        val transform = WCSTransform(keywords)

        val fitsPath = Path.of("src/test/resources/$testName.fits")
        val coords = Array(skyToPix.size * 2) { skyToPix[it / 2][it % 2] }
        val process = ProcessBuilder("sky2xy", "$fitsPath", *coords).start()

        for (line in process.inputStream.bufferedReader().lines()) {
            val parts = line.replace("->", "").split(WHITESPACE_REGEX)
            val rightAscension = parts[0].hours
            val declination = parts[1].deg
            val x0 = parts[3].toDouble()
            val y0 = parts[4].toDouble()
            val (x1, y1) = transform.skyToPix(rightAscension, declination)
            x0 shouldBe (x1 plusOrMinus 0.1)
            y0 shouldBe (y1 plusOrMinus 0.1)
        }

        transform.close()
    }

    private fun readHeaderFromFits(name: String): Header {
        return resource("$name.fits")!!.let(::Fits).use { it.readHDU().header }
    }

    companion object {

        const val EPSILON = 1 / 3600000.0

        @JvmStatic private val WHITESPACE_REGEX = "\\s+".toRegex()

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
