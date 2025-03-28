import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.format.ReadableHeader
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.test.LinuxOnly
import nebulosa.test.download
import nebulosa.wcs.LibWCS
import nebulosa.wcs.WCS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Path

// https://www.atnf.csiro.au/people/mcalabre/WCS/example_data.html

@LinuxOnly
class LibWCSTest {

    @Test
    fun air() {
        pixToSky("AIR")
    }

    @Test
    fun ait() {
        pixToSky("AIT")
    }

    @Test
    fun arc() {
        pixToSky("ARC")
    }

    @Test
    fun azp() {
        pixToSky("AZP")
    }

    @Test
    fun car() {
        pixToSky("CAR")
    }

    @Test
    fun cea() {
        pixToSky("CEA")
    }

    @Test
    fun csc() {
        pixToSky("CSC")
    }

    @Test
    fun cyp() {
        pixToSky("CYP")
    }

    @Test
    fun hpx() {
        pixToSky("HPX")
    }

    @Test
    fun mer() {
        pixToSky("MER")
    }

    @Test
    fun mol() {
        pixToSky("MOL")
    }

    @Test
    fun ncp() {
        pixToSky("NCP")
    }

    @Test
    fun par() {
        pixToSky("PAR")
    }

    @Test
    fun pco() {
        pixToSky("PCO")
    }

    @Test
    fun qsc() {
        pixToSky("QSC")
    }

    @Test
    fun sfl() {
        pixToSky("SFL")
    }

    @Test
    fun sin() {
        pixToSky("SIN")
    }

    @Test
    fun stg() {
        pixToSky("STG")
    }

    @Test
    fun szp() {
        pixToSky("SZP")
    }

    @Test
    fun tan() {
        pixToSky("TAN")
    }

    @Test
    fun tsc() {
        pixToSky("TSC")
    }

    @Test
    fun zea() {
        pixToSky("ZEA")
    }

    @Test
    fun bon() {
        pixToSky("BON")
    }

    @Test
    fun cod() {
        pixToSky("COD")
    }

    @Test
    fun coe() {
        pixToSky("COE")
    }

    @Test
    fun cop() {
        pixToSky("COP")
    }

    @Test
    fun coo() {
        pixToSky("COO")
    }

    @Test
    fun zpn() {
        pixToSky("ZPN")
    }

    @Test
    fun tanSip() {
        // pixToSky("TAN-SIP", 1280, 720)
    }

    companion object {

        @JvmStatic private val CENTER_RA = "18h 59m 51s".hours
        @JvmStatic private val CENTER_DEC = "-66d 15m 57s".deg

        @BeforeAll
        @JvmStatic
        fun loadLibWCS() {
            val libPath = download("https://github.com/tiagohm/nebulosa.data/raw/main/libs/wcs/linux-x86-64/libwcs.so")
            System.setProperty(LibWCS.PATH, "$libPath")
        }

        private fun pixToSky(projectionName: String) {
            WCS(readHeaderFromFits(projectionName)).use {
                val (rightAscension, declination) = it.pixToSky(97.0, 97.0)
                rightAscension shouldBe (CENTER_RA plusOrMinus 1e-12)
                declination shouldBe (CENTER_DEC plusOrMinus 1e-12)

                val (x, y) = it.skyToPix(rightAscension, declination)

                x shouldBe (97.0 plusOrMinus 1e-8)
                y shouldBe (97.0 plusOrMinus 1e-8)
            }
        }

        private fun readHeaderFromFits(name: String): ReadableHeader {
            return Path.of("src/test/resources/$name.fits").fits().use { it.first().header }
        }
    }
}
