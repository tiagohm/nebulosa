package nebulosa.test

import io.kotest.core.spec.style.StringSpec
import nebulosa.fits.Fits
import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.createParentDirectories
import kotlin.io.path.readBytes

@Suppress("PropertyName")
abstract class FitsStringSpec : StringSpec() {

    protected val NGC3344_COLOR_8 by lazy { Fits("$FITS_DIR/NGC3344.Color.8.fits").also(Fits::read) }
    protected val NGC3344_COLOR_16 by lazy { Fits("$FITS_DIR/NGC3344.Color.16.fits").also(Fits::read) }
    protected val NGC3344_COLOR_32 by lazy { Fits("$FITS_DIR/NGC3344.Color.32.fits").also(Fits::read) }
    protected val NGC3344_COLOR_F32 by lazy { Fits("$FITS_DIR/NGC3344.Color.F32.fits").also(Fits::read) }
    protected val NGC3344_COLOR_F64 by lazy { Fits("$FITS_DIR/NGC3344.Color.F64.fits").also(Fits::read) }
    protected val NGC3344_MONO_8 by lazy { Fits("$FITS_DIR/NGC3344.Mono.8.fits").also(Fits::read) }
    protected val NGC3344_MONO_16 by lazy { Fits("$FITS_DIR/NGC3344.Mono.16.fits").also(Fits::read) }
    protected val NGC3344_MONO_32 by lazy { Fits("$FITS_DIR/NGC3344.Mono.32.fits").also(Fits::read) }
    protected val NGC3344_MONO_F32 by lazy { Fits("$FITS_DIR/NGC3344.Mono.F32.fits").also(Fits::read) }
    protected val NGC3344_MONO_F64 by lazy { Fits("$FITS_DIR/NGC3344.Mono.F64.fits").also(Fits::read) }

    protected fun BufferedImage.save(name: String): Pair<Path, String> {
        val path = Path.of("src", "test", "resources", "saved", "$name.png").createParentDirectories()
        ImageIO.write(this, "PNG", path.toFile())
        return path to path.md5()
    }

    internal fun Path.md5(): String {
        return readBytes().toByteString().md5().hex()
    }

    companion object {

        const val FITS_DIR = "../nebulosa-test/src/main/resources/fits"
    }
}
