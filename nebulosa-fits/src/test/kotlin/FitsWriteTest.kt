import io.kotest.matchers.shouldBe
import nebulosa.fits.FitsFormat
import nebulosa.fits.fits
import nebulosa.image.format.ImageHdu
import nebulosa.io.sink
import nebulosa.io.source
import nebulosa.test.AbstractTest
import nebulosa.test.NGC3344_MONO_8_FITS
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test

class FitsWriteTest : AbstractTest() {

    @Test
    fun mono() {
        val hdu0 = NGC3344_MONO_8_FITS.fits().autoClose().filterIsInstance<ImageHdu>().first()
        val data = ByteArray(69120)
        FitsFormat.write(data.sink(), listOf(hdu0))
        data.toByteString(2880, 66240).md5().hex() shouldBe "e1735e21c94dc49885fabc429406e573"

        val fits = data.source().use { it.fits() }
        val hdu1 = fits.filterIsInstance<ImageHdu>().first()

        hdu0.header shouldBe hdu1.header
    }
}
