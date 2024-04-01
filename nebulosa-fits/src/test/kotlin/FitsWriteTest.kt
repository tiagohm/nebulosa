import io.kotest.matchers.shouldBe
import nebulosa.fits.FitsFormat
import nebulosa.fits.fits
import nebulosa.image.format.ImageHdu
import nebulosa.io.sink
import nebulosa.io.source
import nebulosa.test.AbstractFitsAndXisfTest
import okio.ByteString.Companion.toByteString

class FitsWriteTest : AbstractFitsAndXisfTest() {

    init {
        "mono" {
            val hdu0 = NGC3344_MONO_8_FITS.fits().filterIsInstance<ImageHdu>().first()
            val data = ByteArray(69120)
            FitsFormat.write(data.sink(), listOf(hdu0))
            data.toByteString(2880, 66240).md5().hex() shouldBe "e1735e21c94dc49885fabc429406e573"

            val fits = data.source().use { it.fits() }
            val hdu1 = fits.filterIsInstance<ImageHdu>().first()

            hdu0.header shouldBe hdu1.header
        }
    }
}
