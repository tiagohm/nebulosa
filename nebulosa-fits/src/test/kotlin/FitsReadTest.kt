import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.fits.Bitpix
import nebulosa.fits.ImageHdu
import nebulosa.test.FitsStringSpec

class FitsReadTest : FitsStringSpec() {

    init {
        "mono:8-bit" {
            val hdu = NGC3344_MONO_8_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.BYTE
        }
        "mono:16-bit" {
            val hdu = NGC3344_MONO_16_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.SHORT
        }
        "mono:32-bit" {
            val hdu = NGC3344_MONO_32_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.INTEGER
        }
        "mono:32-bit floating-point" {
            val hdu = NGC3344_MONO_F32_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.FLOAT
        }
        "mono:64-bit floating-point" {
            val hdu = NGC3344_MONO_F64_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.DOUBLE
        }
        "color:8-bit" {
            val hdu = NGC3344_COLOR_8_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.BYTE
        }
        "color:16-bit" {
            val hdu = NGC3344_COLOR_16_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.SHORT
        }
        "color:32-bit" {
            val hdu = NGC3344_COLOR_32_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.INTEGER
        }
        "color:32-bit floating-point" {
            val hdu = NGC3344_COLOR_F32_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.FLOAT
        }
        "color:64-bit floating-point" {
            val hdu = NGC3344_COLOR_F64_FITS.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 256
            hdu.height shouldBeExactly 256
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.DOUBLE
        }
    }
}
