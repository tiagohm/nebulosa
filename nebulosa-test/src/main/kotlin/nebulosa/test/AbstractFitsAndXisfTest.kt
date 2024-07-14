package nebulosa.test

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestScope
import nebulosa.hips2fits.Hips2FitsService
import nebulosa.hips2fits.HipsSurvey
import nebulosa.image.format.ImageHdu
import nebulosa.io.transferAndCloseOutput
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.hours
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.io.Closeable
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.*

@Suppress("PropertyName")
abstract class AbstractFitsAndXisfTest : StringSpec() {

    protected val M82_MONO_8_LZ4_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.LZ4.xisf") }
    protected val M82_MONO_8_LZ4_HC_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.LZ4-HC.xisf") }
    protected val M82_MONO_8_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.xisf") }
    protected val M82_MONO_8_ZLIB_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.ZLib.xisf") }
    protected val M82_MONO_8_ZSTANDARD_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.8.ZStandard.xisf") }
    protected val M82_MONO_16_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.16.xisf") }
    protected val M82_MONO_32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.32.xisf") }
    protected val M82_MONO_F32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.F32.xisf") }
    protected val M82_MONO_F64_XISF by lazy { download("$GITHUB_XISF_URL/M82.Mono.F64.xisf") }

    protected val M82_COLOR_8_LZ4_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.LZ4.xisf") }
    protected val M82_COLOR_8_LZ4_HC_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.LZ4-HC.xisf") }
    protected val M82_COLOR_8_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.xisf") }
    protected val M82_COLOR_8_ZLIB_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.ZLib.xisf") }
    protected val M82_COLOR_8_ZSTANDARD_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.8.ZStandard.xisf") }
    protected val M82_COLOR_16_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.16.xisf") }
    protected val M82_COLOR_32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.32.xisf") }
    protected val M82_COLOR_F32_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.F32.xisf") }
    protected val M82_COLOR_F64_XISF by lazy { download("$GITHUB_XISF_URL/M82.Color.F64.xisf") }
    protected val DEBAYER_XISF_PATH by lazy { download("$GITHUB_XISF_URL/Debayer.xisf") }

    protected val NGC3344_MONO_8_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.8.fits") }
    protected val NGC3344_MONO_16_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.16.fits") }
    protected val NGC3344_MONO_32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.32.fits") }
    protected val NGC3344_MONO_F32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.F32.fits") }
    protected val NGC3344_MONO_F64_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Mono.F64.fits") }

    protected val NGC3344_COLOR_8_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.8.fits") }
    protected val NGC3344_COLOR_16_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.16.fits") }
    protected val NGC3344_COLOR_32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.32.fits") }
    protected val NGC3344_COLOR_F32_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.F32.fits") }
    protected val NGC3344_COLOR_F64_FITS by lazy { download("$GITHUB_FITS_URL/NGC3344.Color.F64.fits") }

    protected val PALETTE_MONO_8_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.8.fits") }
    protected val PALETTE_MONO_16_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.16.fits") }
    protected val PALETTE_MONO_32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.32.fits") }
    protected val PALETTE_MONO_F32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.F32.fits") }
    protected val PALETTE_MONO_F64_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Mono.F64.fits") }

    protected val PALETTE_COLOR_8_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.8.fits") }
    protected val PALETTE_COLOR_16_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.16.fits") }
    protected val PALETTE_COLOR_32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.32.fits") }
    protected val PALETTE_COLOR_F32_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.F32.fits") }
    protected val PALETTE_COLOR_F64_FITS by lazy { download("$GITHUB_FITS_URL/PALETTE.Color.F64.fits") }

    protected val PALETTE_MONO_8_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.8.xisf") }
    protected val PALETTE_MONO_16_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.16.xisf") }
    protected val PALETTE_MONO_32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.32.xisf") }
    protected val PALETTE_MONO_F32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.F32.xisf") }
    protected val PALETTE_MONO_F64_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Mono.F64.xisf") }

    protected val PALETTE_COLOR_8_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.8.xisf") }
    protected val PALETTE_COLOR_16_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.16.xisf") }
    protected val PALETTE_COLOR_32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.32.xisf") }
    protected val PALETTE_COLOR_F32_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.F32.xisf") }
    protected val PALETTE_COLOR_F64_XISF by lazy { download("$GITHUB_XISF_URL/PALETTE.Color.F64.xisf") }

    protected val DEBAYER_FITS by lazy { download("$GITHUB_FITS_URL/Debayer.fits") }
    protected val M6707HH by lazy { download("$ASTROPY_PHOTOMETRY_URL/M6707HH.fits") }
    protected val M31_FITS by lazy { download("00 42 44.3".hours, "41 16 9".deg, 3.deg) }

    protected val STAR_FOCUS_1 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.1.fits") }
    protected val STAR_FOCUS_2 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.2.fits") }
    protected val STAR_FOCUS_3 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.3.fits") }
    protected val STAR_FOCUS_4 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.4.fits") }
    protected val STAR_FOCUS_5 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.5.fits") }
    protected val STAR_FOCUS_6 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.6.fits") }
    protected val STAR_FOCUS_7 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.7.fits") }
    protected val STAR_FOCUS_8 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.8.fits") }
    protected val STAR_FOCUS_9 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.9.fits") }
    protected val STAR_FOCUS_10 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.10.fits") }
    protected val STAR_FOCUS_11 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.11.fits") }
    protected val STAR_FOCUS_12 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.12.fits") }
    protected val STAR_FOCUS_13 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.13.fits") }
    protected val STAR_FOCUS_14 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.14.fits") }
    protected val STAR_FOCUS_15 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.15.fits") }
    protected val STAR_FOCUS_16 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.16.fits") }
    protected val STAR_FOCUS_17 by lazy { download("$GITHUB_FITS_URL/STAR.FOCUS.17.fits") }

    protected val PI_01_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.01.LIGHT.fits") }
    protected val PI_02_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.02.LIGHT.fits") }
    protected val PI_03_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.03.LIGHT.fits") }
    protected val PI_04_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.04.LIGHT.fits") }
    protected val PI_05_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.05.LIGHT.fits") }
    protected val PI_06_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.06.LIGHT.fits") }
    protected val PI_07_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.07.LIGHT.fits") }
    protected val PI_08_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.08.LIGHT.fits") }
    protected val PI_09_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.09.LIGHT.fits") }
    protected val PI_10_LIGHT by lazy { download("$GITHUB_FITS_URL/PI.10.LIGHT.fits") }
    protected val PI_BIAS by lazy { download("$GITHUB_FITS_URL/PI.BIAS.fits") }
    protected val PI_DARK by lazy { download("$GITHUB_FITS_URL/PI.DARK.fits") }
    protected val PI_FLAT by lazy { download("$GITHUB_FITS_URL/PI.FLAT.fits") }

    protected val PI_FOCUS_0 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.0.fits") }
    protected val PI_FOCUS_10000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.10000.fits") }
    protected val PI_FOCUS_20000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.20000.fits") }
    protected val PI_FOCUS_30000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.30000.fits") }
    protected val PI_FOCUS_100000 by lazy { download("$GITHUB_FITS_URL/PI.FOCUS.100000.fits") }

    private val afterEach = AfterEach()

    init {
        prependExtension(afterEach)
    }

    protected fun <T : Closeable> TestScope.closeAfterEach(closeable: T) = closeable.apply {
        afterEach.add(testCase to this)
    }

    protected fun BufferedImage.save(name: String): Pair<Path, String> {
        val path = Path.of("..", "data", "test", "$name.png").createParentDirectories()
        ImageIO.write(this, "PNG", path.toFile())
        val md5 = path.md5()
        println("$name: $md5")
        return path to md5
    }

    protected fun ByteArray.md5() = toByteString().md5().hex()

    protected fun Path.md5() = readBytes().md5()

    protected val String.extensionFromUrl
        get() = if (endsWith(".fits", true)) "fits"
        else if (endsWith(".xisf", true)) "xisf"
        else ""

    protected fun download(url: String, extension: String = url.extensionFromUrl): Path {
        require(extension.isNotBlank())

        return synchronize(url) {
            val name = url.toByteArray().md5()
            val path = Path.of(System.getProperty("java.io.tmpdir"), "$name.$extension")

            if (!path.exists() || path.fileSize() <= 0L) {
                val request = Request.Builder().get().url(url).build()
                val call = HTTP_CLIENT.newCall(request)

                call.execute().use {
                    it.body?.byteStream()?.transferAndCloseOutput(path.outputStream())
                }
            }

            path
        }
    }

    protected fun download(centerRA: Angle, centerDEC: Angle, fov: Angle): Path {
        val name = "$centerRA@$centerDEC@$fov".toByteArray().md5()

        return synchronize(name) {
            val path = Path.of(System.getProperty("java.io.tmpdir"), name)

            if (!path.exists() || path.fileSize() <= 0L) {
                HIPS_SERVICE
                    .query(CDS_P_DSS2_NIR.id, centerRA, centerDEC, 1280, 720, 0.0, fov)
                    .execute()
                    .body()!!
                    .use { it.byteStream().transferAndCloseOutput(path.outputStream()) }
            }

            path
        }
    }

    protected fun ImageHdu.makeImage(): BufferedImage {
        val type = if (numberOfChannels == 1) BufferedImage.TYPE_BYTE_GRAY else BufferedImage.TYPE_INT_RGB
        val image = BufferedImage(width, height, type)
        val numberOfPixels = data.numberOfPixels

        if (numberOfChannels == 1) {
            val buffer = (image.raster.dataBuffer as DataBufferByte).data

            repeat(numberOfPixels) {
                buffer[it] = (data.red[it] * 255f).toInt().toByte()
            }
        } else {
            val buffer = (image.raster.dataBuffer as DataBufferInt).data

            repeat(numberOfPixels) {
                val red = (data.red[it] * 255f).toInt() and 0xFF
                val green = (data.green[it] * 255f).toInt() and 0xFF
                val blue = (data.blue[it] * 255f).toInt() and 0xFF
                buffer[it] = blue or (green shl 8) or (red shl 16)
            }
        }

        return image
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private class AfterEach : ConcurrentLinkedDeque<Pair<TestCase, Closeable>>(), TestListener {

        override suspend fun afterEach(testCase: TestCase, result: TestResult) {
            find { it.first === testCase }?.second?.close() ?: return
        }
    }

    companion object {

        const val ASTROPY_PHOTOMETRY_URL = "https://www.astropy.org/astropy-data/photometry"
        const val GITHUB_FITS_URL = "https://github.com/tiagohm/nebulosa.data/raw/main/test/fits"
        const val GITHUB_XISF_URL = "https://github.com/tiagohm/nebulosa.data/raw/main/test/xisf"

        @JvmStatic val HTTP_CLIENT = OkHttpClient.Builder()
            .readTimeout(60L, TimeUnit.SECONDS)
            .writeTimeout(60L, TimeUnit.SECONDS)
            .connectTimeout(60L, TimeUnit.SECONDS)
            .callTimeout(60L, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()

        @JvmStatic val HIPS_SERVICE = Hips2FitsService(httpClient = HTTP_CLIENT)
        @JvmStatic val CDS_P_DSS2_NIR = HipsSurvey("CDS/P/DSS2/NIR")
        @JvmStatic @PublishedApi internal val SYNC_KEYS = ConcurrentHashMap<String, Any>()

        inline fun <R> synchronize(key: String, block: () -> R): R {
            val lock = synchronized(SYNC_KEYS) { SYNC_KEYS.getOrPut(key) { Any() } }
            return synchronized(lock, block)
        }
    }
}
