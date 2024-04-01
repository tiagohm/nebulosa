package nebulosa.test

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestScope
import nebulosa.fits.FitsPath
import nebulosa.fits.fits
import nebulosa.io.transferAndCloseOutput
import nebulosa.xisf.XisfPath
import nebulosa.xisf.xisf
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString.Companion.toByteString
import java.awt.image.BufferedImage
import java.io.Closeable
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.*

@Suppress("PropertyName")
abstract class FitsStringSpec : StringSpec() {

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

    protected val DEBAYER_FITS_PATH by lazy { download("Debayer.fits", GITHUB_FITS_URL) }
    protected val M6707HH by lazyFITS("M6707HH.fits", ASTROPY_PHOTOMETRY_URL)

    protected val STAR_FOCUS_1 by lazyFITS("STAR.FOCUS.1.fits")
    protected val STAR_FOCUS_2 by lazyFITS("STAR.FOCUS.2.fits")
    protected val STAR_FOCUS_3 by lazyFITS("STAR.FOCUS.3.fits")
    protected val STAR_FOCUS_4 by lazyFITS("STAR.FOCUS.4.fits")
    protected val STAR_FOCUS_5 by lazyFITS("STAR.FOCUS.5.fits")
    protected val STAR_FOCUS_6 by lazyFITS("STAR.FOCUS.6.fits")
    protected val STAR_FOCUS_7 by lazyFITS("STAR.FOCUS.7.fits")
    protected val STAR_FOCUS_8 by lazyFITS("STAR.FOCUS.8.fits")
    protected val STAR_FOCUS_9 by lazyFITS("STAR.FOCUS.9.fits")
    protected val STAR_FOCUS_10 by lazyFITS("STAR.FOCUS.10.fits")
    protected val STAR_FOCUS_11 by lazyFITS("STAR.FOCUS.11.fits")
    protected val STAR_FOCUS_12 by lazyFITS("STAR.FOCUS.12.fits")
    protected val STAR_FOCUS_13 by lazyFITS("STAR.FOCUS.13.fits")
    protected val STAR_FOCUS_14 by lazyFITS("STAR.FOCUS.14.fits")
    protected val STAR_FOCUS_15 by lazyFITS("STAR.FOCUS.15.fits")
    protected val STAR_FOCUS_16 by lazyFITS("STAR.FOCUS.16.fits")
    protected val STAR_FOCUS_17 by lazyFITS("STAR.FOCUS.17.fits")

    private val afterEach = AfterEach()

    init {
        prependExtension(afterEach)
    }

    protected fun <T : Closeable> TestScope.closeAfterEach(closeable: T) = closeable.apply {
        afterEach[testCase] = this
    }

    protected fun BufferedImage.save(name: String): Pair<Path, String> {
        val path = Path.of("..", "data", "test", "$name.png").createParentDirectories()
        ImageIO.write(this, "PNG", path.toFile())
        val md5 = path.md5()
        println("$name: $md5")
        return path to md5
    }

    protected fun Path.md5(): String {
        return readBytes().toByteString().md5().hex()
    }

    protected fun lazyFITS(name: String, baseUrl: String = GITHUB_FITS_URL): Lazy<FitsPath> {
        return lazy { download(name, baseUrl).fits() }
    }

    protected fun lazyXISF(name: String, baseUrl: String = GITHUB_XISF_URL): Lazy<XisfPath> {
        return lazy { download(name, baseUrl).xisf() }
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private class AfterEach : ConcurrentHashMap<TestCase, Closeable>(), TestListener {

        override suspend fun afterEach(testCase: TestCase, result: TestResult) {
            remove(testCase)?.close()
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

        @JvmStatic @PublishedApi internal val SYNC_KEYS = ConcurrentHashMap<String, Any>()

        inline fun <R> synchronize(key: String, block: () -> R): R {
            val lock = synchronized(SYNC_KEYS) { SYNC_KEYS.getOrPut(key) { Any() } }
            return synchronized(lock, block)
        }
    }
}
