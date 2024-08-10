import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.fits.Bitpix
import nebulosa.fits.bitpix
import nebulosa.fits.fits
import nebulosa.image.format.ImageHdu
import nebulosa.test.*
import org.junit.jupiter.api.Test

class FitsReadTest {

    @Test
    fun mono8Bit() {
        val hdu = NGC3344_MONO_8_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 1
        hdu.header.bitpix shouldBe Bitpix.BYTE
    }

    @Test
    fun mono16Bit() {
        val hdu = NGC3344_MONO_16_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 1
        hdu.header.bitpix shouldBe Bitpix.SHORT
    }

    @Test
    fun mono32Bit() {
        val hdu = NGC3344_MONO_32_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 1
        hdu.header.bitpix shouldBe Bitpix.INTEGER
    }

    @Test
    fun monoFloat32Bit() {
        val hdu = NGC3344_MONO_F32_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 1
        hdu.header.bitpix shouldBe Bitpix.FLOAT
    }

    @Test
    fun monoFloat64Bit() {
        val hdu = NGC3344_MONO_F64_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 1
        hdu.header.bitpix shouldBe Bitpix.DOUBLE
    }

    @Test
    fun color8Bit() {
        val hdu = NGC3344_COLOR_8_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 4
        hdu.header.bitpix shouldBe Bitpix.BYTE
    }

    @Test
    fun color16Bit() {
        val hdu = NGC3344_COLOR_16_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 4
        hdu.header.bitpix shouldBe Bitpix.SHORT
    }

    @Test
    fun color32Bit() {
        val hdu = NGC3344_COLOR_32_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 4
        hdu.header.bitpix shouldBe Bitpix.INTEGER
    }

    @Test
    fun colorFloat32Bit() {
        val hdu = NGC3344_COLOR_F32_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 4
        hdu.header.bitpix shouldBe Bitpix.FLOAT
    }

    @Test
    fun colorFloat64Bit() {
        val hdu = NGC3344_COLOR_F64_FITS.fits().use { it.filterIsInstance<ImageHdu>().first() }
        hdu.width shouldBeExactly 256
        hdu.height shouldBeExactly 256
        hdu.numberOfChannels shouldBeExactly 4
        hdu.header.bitpix shouldBe Bitpix.DOUBLE
    }
}
