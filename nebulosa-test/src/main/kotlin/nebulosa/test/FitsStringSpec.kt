package nebulosa.test

import io.kotest.core.spec.style.StringSpec
import nebulosa.fits.Fits
import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Suppress("LeakingThis", "PropertyName")
abstract class FitsStringSpec : StringSpec() {

    @JvmField protected val NGC3344_COLOR_8 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Color.8.fits")
    @JvmField protected val NGC3344_COLOR_16 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Color.16.fits")
    @JvmField protected val NGC3344_COLOR_32 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Color.32.fits")
    @JvmField protected val NGC3344_COLOR_F32 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Color.F32.fits")
    @JvmField protected val NGC3344_COLOR_F64 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Color.F64.fits")
    @JvmField protected val NGC3344_MONO_8 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Mono.8.fits")
    @JvmField protected val NGC3344_MONO_16 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Mono.16.fits")
    @JvmField protected val NGC3344_MONO_32 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Mono.32.fits")
    @JvmField protected val NGC3344_MONO_F32 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Mono.F32.fits")
    @JvmField protected val NGC3344_MONO_F64 = Fits("../nebulosa-test/src/main/resources/fits/NGC3344.Mono.F64.fits")

    init {
        beforeSpec {
            NGC3344_COLOR_8.read()
            NGC3344_COLOR_16.read()
            NGC3344_COLOR_32.read()
            NGC3344_COLOR_F32.read()
            NGC3344_COLOR_F64.read()
            NGC3344_MONO_8.read()
            NGC3344_MONO_16.read()
            NGC3344_MONO_32.read()
            NGC3344_MONO_F32.read()
            NGC3344_MONO_F64.read()
        }

        afterSpec {
            NGC3344_COLOR_8.close()
            NGC3344_COLOR_16.close()
            NGC3344_COLOR_32.close()
            NGC3344_COLOR_F32.close()
            NGC3344_COLOR_F64.close()
            NGC3344_MONO_8.close()
            NGC3344_MONO_16.close()
            NGC3344_MONO_32.close()
            NGC3344_MONO_F32.close()
            NGC3344_MONO_F64.close()
        }
    }

    protected fun BufferedImage.save(name: String): Pair<File, String> {
        val file = File("src/test/resources/saved/$name.png")
        ImageIO.write(this, "PNG", file)
        return file to file.md5()
    }

    internal fun File.md5(): String {
        return readBytes().toByteString().md5().hex()
    }
}
