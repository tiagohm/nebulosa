import io.kotest.matchers.shouldBe
import nebulosa.fits.ImageHdu
import nebulosa.fits.fits
import nebulosa.io.sink
import nebulosa.io.source
import nebulosa.test.FitsStringSpec
import okio.ByteString.Companion.toByteString

class FitsWriteTest : FitsStringSpec() {

    init {
        "mono" {
            val hdu0 = NGC3344_MONO_8.filterIsInstance<ImageHdu>().first()
            val data = ByteArray(69120)
            hdu0.write(data.sink())
            data.toByteString(2880, 66240).md5().hex() shouldBe "e1735e21c94dc49885fabc429406e573"

            val fits = data.source().fits()
            val hdu1 = fits.filterIsInstance<ImageHdu>().first()

            hdu0.header shouldBe hdu1.header
        }
    }
}
