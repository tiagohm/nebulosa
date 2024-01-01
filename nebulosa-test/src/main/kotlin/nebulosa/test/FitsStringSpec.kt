package nebulosa.test

import io.kotest.core.spec.style.StringSpec
import nebulosa.fits.Fits
import nebulosa.io.transferAndCloseOutput
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import kotlin.io.path.*


@Suppress("PropertyName")
abstract class FitsStringSpec : StringSpec() {

    protected val FITS_DIR = "../data/fits"

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
    protected val M6707HH by lazy { Fits(download("M6707HH.fits", ASTROPY_PHOTOMETRY_URL)).also(Fits::read) }
    protected val STAR_FOCUS_1 by lazy { Fits("$FITS_DIR/STAR_FOCUS_1.fits").also(Fits::read) }
    protected val STAR_FOCUS_2 by lazy { Fits("$FITS_DIR/STAR_FOCUS_2.fits").also(Fits::read) }
    protected val STAR_FOCUS_3 by lazy { Fits("$FITS_DIR/STAR_FOCUS_3.fits").also(Fits::read) }
    protected val STAR_FOCUS_4 by lazy { Fits("$FITS_DIR/STAR_FOCUS_4.fits").also(Fits::read) }
    protected val STAR_FOCUS_5 by lazy { Fits("$FITS_DIR/STAR_FOCUS_5.fits").also(Fits::read) }
    protected val STAR_FOCUS_6 by lazy { Fits("$FITS_DIR/STAR_FOCUS_6.fits").also(Fits::read) }
    protected val STAR_FOCUS_7 by lazy { Fits("$FITS_DIR/STAR_FOCUS_7.fits").also(Fits::read) }
    protected val STAR_FOCUS_8 by lazy { Fits("$FITS_DIR/STAR_FOCUS_8.fits").also(Fits::read) }
    protected val STAR_FOCUS_9 by lazy { Fits("$FITS_DIR/STAR_FOCUS_9.fits").also(Fits::read) }
    protected val STAR_FOCUS_10 by lazy { Fits("$FITS_DIR/STAR_FOCUS_10.fits").also(Fits::read) }
    protected val STAR_FOCUS_11 by lazy { Fits("$FITS_DIR/STAR_FOCUS_11.fits").also(Fits::read) }
    protected val STAR_FOCUS_12 by lazy { Fits("$FITS_DIR/STAR_FOCUS_12.fits").also(Fits::read) }
    protected val STAR_FOCUS_13 by lazy { Fits("$FITS_DIR/STAR_FOCUS_13.fits").also(Fits::read) }
    protected val STAR_FOCUS_14 by lazy { Fits("$FITS_DIR/STAR_FOCUS_14.fits").also(Fits::read) }
    protected val STAR_FOCUS_15 by lazy { Fits("$FITS_DIR/STAR_FOCUS_15.fits").also(Fits::read) }
    protected val STAR_FOCUS_16 by lazy { Fits("$FITS_DIR/STAR_FOCUS_16.fits").also(Fits::read) }
    protected val STAR_FOCUS_17 by lazy { Fits("$FITS_DIR/STAR_FOCUS_17.fits").also(Fits::read) }
    protected val UNCALIBRATED by lazy { Fits("$FITS_DIR/UNCALIBRATED.fits").also(Fits::read) }
    protected val DARK by lazy { Fits("$FITS_DIR/DARK.fits").also(Fits::read) }
    protected val FLAT by lazy { Fits("$FITS_DIR/FLAT.fits").also(Fits::read) }
    protected val BIAS by lazy { Fits("$FITS_DIR/BIAS.fits").also(Fits::read) }

    protected fun BufferedImage.save(name: String): Pair<Path, String> {
        val path = Path.of("src", "test", "resources", "saved", "$name.png").createParentDirectories()
        ImageIO.write(this, "PNG", path.toFile())
        return path to path.md5()
    }

    protected fun Path.md5(): String {
        return readBytes().toByteString().md5().hex()
    }

    protected fun download(name: String, baseUrl: String): Path {
        val path = Path.of(System.getProperty("java.io.tmpdir"), name)

        if (path.exists() && path.fileSize() > 0L) {
            return path
        }

        val request = Request.Builder()
            .get()
            .url("$baseUrl/$name")
            .build()

        val call = HTTP_CLIENT.newCall(request)

        call.execute().use {
            it.body?.byteStream()?.transferAndCloseOutput(path.outputStream())
        }

        return path
    }

    protected fun unzip(name: String) {
        val path = Path.of(FITS_DIR, "$name.zip")

        with(ZipInputStream(path.inputStream())) {
            var zipEntry = getNextEntry()

            while (zipEntry != null) {
                transferAndCloseOutput(Path.of(FITS_DIR, zipEntry.name).outputStream())
                zipEntry = getNextEntry()
            }

            closeEntry()
            close()
        }
    }

    companion object {

        const val ASTROPY_PHOTOMETRY_URL = "https://www.astropy.org/astropy-data/photometry"

        @JvmStatic val HTTP_CLIENT = OkHttpClient.Builder()
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .callTimeout(60L, TimeUnit.SECONDS)
            .build()
    }
}
