package nebulosa.api.atlas

import com.sun.jna.Platform
import nebulosa.api.preference.PreferenceService
import nebulosa.io.transferAndCloseOutput
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.wcs.LibWCS
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Path
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.outputStream

class LibWCSDownloadTask(
    private val libsPath: Path,
    private val httpClient: OkHttpClient,
    private val preferenceService: PreferenceService,
    scheduledExecutorService: ScheduledExecutorService,
) : Runnable {

    init {
        scheduledExecutorService.schedule(this, 5L, TimeUnit.SECONDS)
    }

    override fun run() {
        var request = Request.Builder().get().url(VERSION_URL).build()

        val libraryUrl = checkNotNull(LIBRARY_URLS[Platform.RESOURCE_PREFIX])
        val libraryPath = Path.of("$libsPath", libraryUrl.substring(libraryUrl.lastIndexOf('/') + 1))

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val newestVersion = response.body!!.string()

                if (newestVersion != preferenceService.getText(VERSION_KEY) || !libraryPath.exists()) {
                    LOG.i("LibWCS is out of date. Downloading...")

                    request = Request.Builder().get().url(libraryUrl).build()

                    httpClient.newCall(request).execute().use {
                        if (it.isSuccessful) {
                            it.body!!.byteStream().transferAndCloseOutput(libraryPath.outputStream())
                            preferenceService.putText(VERSION_KEY, newestVersion)
                        }
                    }
                } else {
                    LOG.i("LibWCS is up to date. version={}", newestVersion)
                }
            }
        }

        System.setProperty(LibWCS.PATH, "$libraryPath")
    }

    companion object {

        const val VERSION_URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/VERSION.txt"
        const val VERSION_KEY = "LIBWCS.VERSION"

        @JvmStatic private val LOG = loggerFor<LibWCSDownloadTask>()

        @JvmStatic private val LIBRARY_URLS = mapOf(
            "linux-x86-64" to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/linux-x86-64/libwcs.so",
            "win32-x86-64" to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/win32-x86-64/libwcs.dll",
        )
    }
}
