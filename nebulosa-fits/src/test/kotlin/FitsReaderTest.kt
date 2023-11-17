import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.fits.Bitpix
import nebulosa.fits.ImageHdu
import nebulosa.test.FitsStringSpec

class FitsReaderTest : FitsStringSpec() {

    init {
        "mono 8-bit" {
            val hdu = m8bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.BYTE
        }
        "mono 16-bit" {
            val hdu = m16bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.SHORT
        }
        "mono 32-bit" {
            val hdu = m32bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.INTEGER
        }
        "mono 32-bit float" {
            val hdu = mF32bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.FLOAT
        }
        "mono 64-bit float" {
            val hdu = mF64bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 1
            hdu.bitpix shouldBe Bitpix.DOUBLE
        }
        "color 8-bit" {
            val hdu = c8bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.BYTE
        }
        "color 16-bit" {
            val hdu = c16bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.SHORT
        }
        "color 32-bit" {
            val hdu = c32bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.INTEGER
        }
        "color 32-bit float" {
            val hdu = cF32bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.FLOAT
        }
        "color 64-bit float" {
            val hdu = cF64bit.filterIsInstance<ImageHdu>().first()
            hdu.width shouldBeExactly 8
            hdu.height shouldBeExactly 8
            hdu.size shouldBeExactly 4
            hdu.bitpix shouldBe Bitpix.DOUBLE
        }
    }
}
