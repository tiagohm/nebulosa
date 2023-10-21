import io.kotest.core.spec.style.StringSpec
import nebulosa.fits.Fits
import nebulosa.io.seekableSource
import okio.Path.Companion.toPath

class FitsReaderTest : StringSpec() {

    init {
        "read hdu" {
            val source = "/home/tiagohm/Downloads/NGC5128-LRGB.fit".toPath().toFile().seekableSource()
            val fits = Fits()
            fits.read(source)
        }
    }
}
