package nebulosa.test

import io.kotest.core.spec.style.StringSpec
import nebulosa.fits.Fits
import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Suppress("LeakingThis")
abstract class FitsStringSpec : StringSpec() {

    @JvmField protected val m8bit = Fits("../nebulosa-test/src/main/resources/fits/Mono.8.fits")
    @JvmField protected val m16bit = Fits("../nebulosa-test/src/main/resources/fits/Mono.16.fits")
    @JvmField protected val m32bit = Fits("../nebulosa-test/src/main/resources/fits/Mono.32.fits")
    @JvmField protected val mF32bit = Fits("../nebulosa-test/src/main/resources/fits/Mono.F32.fits")
    @JvmField protected val mF64bit = Fits("../nebulosa-test/src/main/resources/fits/Mono.F64.fits")
    @JvmField protected val c8bit = Fits("../nebulosa-test/src/main/resources/fits/Color.8.fits")
    @JvmField protected val c16bit = Fits("../nebulosa-test/src/main/resources/fits/Color.16.fits")
    @JvmField protected val c32bit = Fits("../nebulosa-test/src/main/resources/fits/Color.32.fits")
    @JvmField protected val cF32bit = Fits("../nebulosa-test/src/main/resources/fits/Color.F32.fits")
    @JvmField protected val cF64bit = Fits("../nebulosa-test/src/main/resources/fits/Color.F64.fits")

    init {
        beforeSpec {
            m8bit.read()
            m16bit.read()
            m32bit.read()
            mF32bit.read()
            mF64bit.read()
            c8bit.read()
            c16bit.read()
            c32bit.read()
            cF32bit.read()
            cF64bit.read()
        }

        afterSpec {
            m8bit.close()
            m16bit.close()
            m32bit.close()
            mF32bit.close()
            mF64bit.close()
            c8bit.close()
            c16bit.close()
            c32bit.close()
            cF32bit.close()
            cF64bit.close()
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
