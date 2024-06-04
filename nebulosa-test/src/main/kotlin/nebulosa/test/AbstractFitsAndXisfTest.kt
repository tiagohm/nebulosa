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

    protected val M82_MONO_8_LZ4_XISF by lazy { download("M82.Mono.8.LZ4.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_8_LZ4_HC_XISF by lazy { download("M82.Mono.8.LZ4-HC.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_8_XISF by lazy { download("M82.Mono.8.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_8_ZLIB_XISF by lazy { download("M82.Mono.8.ZLib.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_8_ZSTANDARD_XISF by lazy { download("M82.Mono.8.ZStandard.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_16_XISF by lazy { download("M82.Mono.16.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_32_XISF by lazy { download("M82.Mono.32.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_F32_XISF by lazy { download("M82.Mono.F32.xisf", GITHUB_XISF_URL) }
    protected val M82_MONO_F64_XISF by lazy { download("M82.Mono.F64.xisf", GITHUB_XISF_URL) }

    protected val M82_COLOR_8_LZ4_XISF by lazy { download("M82.Color.8.LZ4.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_8_LZ4_HC_XISF by lazy { download("M82.Color.8.LZ4-HC.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_8_XISF by lazy { download("M82.Color.8.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_8_ZLIB_XISF by lazy { download("M82.Color.8.ZLib.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_8_ZSTANDARD_XISF by lazy { download("M82.Color.8.ZStandard.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_16_XISF by lazy { download("M82.Color.16.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_32_XISF by lazy { download("M82.Color.32.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_F32_XISF by lazy { download("M82.Color.F32.xisf", GITHUB_XISF_URL) }
    protected val M82_COLOR_F64_XISF by lazy { download("M82.Color.F64.xisf", GITHUB_XISF_URL) }
    protected val DEBAYER_XISF_PATH by lazy { download("Debayer.xisf", GITHUB_XISF_URL) }

    protected val NGC3344_MONO_8_FITS by lazy { download("NGC3344.Mono.8.fits", GITHUB_FITS_URL) }
    protected val NGC3344_MONO_16_FITS by lazy { download("NGC3344.Mono.16.fits", GITHUB_FITS_URL) }
    protected val NGC3344_MONO_32_FITS by lazy { download("NGC3344.Mono.32.fits", GITHUB_FITS_URL) }
    protected val NGC3344_MONO_F32_FITS by lazy { download("NGC3344.Mono.F32.fits", GITHUB_FITS_URL) }
    protected val NGC3344_MONO_F64_FITS by lazy { download("NGC3344.Mono.F64.fits", GITHUB_FITS_URL) }

    protected val NGC3344_COLOR_8_FITS by lazy { download("NGC3344.Color.8.fits", GITHUB_FITS_URL) }
    protected val NGC3344_COLOR_16_FITS by lazy { download("NGC3344.Color.16.fits", GITHUB_FITS_URL) }
    protected val NGC3344_COLOR_32_FITS by lazy { download("NGC3344.Color.32.fits", GITHUB_FITS_URL) }
    protected val NGC3344_COLOR_F32_FITS by lazy { download("NGC3344.Color.F32.fits", GITHUB_FITS_URL) }
    protected val NGC3344_COLOR_F64_FITS by lazy { download("NGC3344.Color.F64.fits", GITHUB_FITS_URL) }

    protected val PALETTE_MONO_8_FITS by lazy { download("PALETTE.Mono.8.fits", GITHUB_FITS_URL) }
    protected val PALETTE_MONO_16_FITS by lazy { download("PALETTE.Mono.16.fits", GITHUB_FITS_URL) }
    protected val PALETTE_MONO_32_FITS by lazy { download("PALETTE.Mono.32.fits", GITHUB_FITS_URL) }
    protected val PALETTE_MONO_F32_FITS by lazy { download("PALETTE.Mono.F32.fits", GITHUB_FITS_URL) }
    protected val PALETTE_MONO_F64_FITS by lazy { download("PALETTE.Mono.F64.fits", GITHUB_FITS_URL) }

    protected val PALETTE_COLOR_8_FITS by lazy { download("PALETTE.Color.8.fits", GITHUB_FITS_URL) }
    protected val PALETTE_COLOR_16_FITS by lazy { download("PALETTE.Color.16.fits", GITHUB_FITS_URL) }
    protected val PALETTE_COLOR_32_FITS by lazy { download("PALETTE.Color.32.fits", GITHUB_FITS_URL) }
    protected val PALETTE_COLOR_F32_FITS by lazy { download("PALETTE.Color.F32.fits", GITHUB_FITS_URL) }
    protected val PALETTE_COLOR_F64_FITS by lazy { download("PALETTE.Color.F64.fits", GITHUB_FITS_URL) }

    protected val PALETTE_MONO_8_XISF by lazy { download("PALETTE.Mono.8.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_MONO_16_XISF by lazy { download("PALETTE.Mono.16.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_MONO_32_XISF by lazy { download("PALETTE.Mono.32.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_MONO_F32_XISF by lazy { download("PALETTE.Mono.F32.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_MONO_F64_XISF by lazy { download("PALETTE.Mono.F64.xisf", GITHUB_XISF_URL) }

    protected val PALETTE_COLOR_8_XISF by lazy { download("PALETTE.Color.8.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_COLOR_16_XISF by lazy { download("PALETTE.Color.16.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_COLOR_32_XISF by lazy { download("PALETTE.Color.32.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_COLOR_F32_XISF by lazy { download("PALETTE.Color.F32.xisf", GITHUB_XISF_URL) }
    protected val PALETTE_COLOR_F64_XISF by lazy { download("PALETTE.Color.F64.xisf", GITHUB_XISF_URL) }

    protected val DEBAYER_FITS by lazy { download("Debayer.fits", GITHUB_FITS_URL) }
    protected val M6707HH by lazy { download("M6707HH.fits", ASTROPY_PHOTOMETRY_URL) }
    protected val M31_FITS by lazy { download("00 42 44.3".hours, "41 16 9".deg, 3.deg) }

    protected val STAR_FOCUS_1 by lazy { download("STAR.FOCUS.1.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_2 by lazy { download("STAR.FOCUS.2.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_3 by lazy { download("STAR.FOCUS.3.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_4 by lazy { download("STAR.FOCUS.4.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_5 by lazy { download("STAR.FOCUS.5.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_6 by lazy { download("STAR.FOCUS.6.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_7 by lazy { download("STAR.FOCUS.7.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_8 by lazy { download("STAR.FOCUS.8.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_9 by lazy { download("STAR.FOCUS.9.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_10 by lazy { download("STAR.FOCUS.10.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_11 by lazy { download("STAR.FOCUS.11.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_12 by lazy { download("STAR.FOCUS.12.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_13 by lazy { download("STAR.FOCUS.13.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_14 by lazy { download("STAR.FOCUS.14.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_15 by lazy { download("STAR.FOCUS.15.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_16 by lazy { download("STAR.FOCUS.16.fits", GITHUB_FITS_URL) }
    protected val STAR_FOCUS_17 by lazy { download("STAR.FOCUS.17.fits", GITHUB_FITS_URL) }

    protected val PI_01_LIGHT by lazy { download("PI.01.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_02_LIGHT by lazy { download("PI.02.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_03_LIGHT by lazy { download("PI.03.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_04_LIGHT by lazy { download("PI.04.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_05_LIGHT by lazy { download("PI.05.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_06_LIGHT by lazy { download("PI.06.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_07_LIGHT by lazy { download("PI.07.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_08_LIGHT by lazy { download("PI.08.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_09_LIGHT by lazy { download("PI.09.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_10_LIGHT by lazy { download("PI.10.LIGHT.fits", GITHUB_FITS_URL) }
    protected val PI_BIAS by lazy { download("PI.BIAS.fits", GITHUB_FITS_URL) }
    protected val PI_DARK by lazy { download("PI.DARK.fits", GITHUB_FITS_URL) }
    protected val PI_FLAT by lazy { download("PI.FLAT.fits", GITHUB_FITS_URL) }

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

    protected fun ByteArray.md5(): String {
        return toByteString().md5().hex()
    }

    protected fun Path.md5(): String {
        return readBytes().md5()
    }

    protected fun download(name: String, baseUrl: String): Path {
        return synchronize(name) {
            val path = Path.of(System.getProperty("java.io.tmpdir"), name)

            if (!path.exists() || path.fileSize() <= 0L) {
                val request = Request.Builder()
                    .get()
                    .url("$baseUrl/$name")
                    .build()

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
