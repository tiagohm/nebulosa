package nebulosa.api.atlas

import com.sun.jna.Platform
import nebulosa.api.preferences.PreferenceService
import nebulosa.io.transferAndCloseOutput
import nebulosa.log.loggerFor
import nebulosa.wcs.LibWCS
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.outputStream

@Component
class LibWCSDownloadTask(
    private val httpClient: OkHttpClient,
    private val preferenceService: PreferenceService,
    private val libsPath: Path,
) : Runnable {

    @Scheduled(fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        var request = Request.Builder().get().url(VERSION_URL).build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val newestVersion = response.body!!.string()
                val libraryPath = Path.of("$libsPath", "libwcs")

                if (newestVersion != preferenceService.getText(VERSION_KEY) || !libraryPath.exists()) {
                    LOG.info("LibWCS is out of date. Downloading it...")

                    val libraryUrl = checkNotNull(LIBRARY_URLS[Platform.RESOURCE_PREFIX])
                    request = Request.Builder().get().url(libraryUrl).build()

                    httpClient.newCall(request).execute().use {
                        if (it.isSuccessful) {
                            it.body!!.byteStream().transferAndCloseOutput(libraryPath.outputStream())
                            System.setProperty(LibWCS.PATH, "$libraryPath")
                            preferenceService.putText(VERSION_KEY, newestVersion)
                        }
                    }
                } else {
                    LOG.info("LibWCS is up to date. version=$newestVersion")
                }
            }
        }
    }

    companion object {

        const val VERSION_URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/wcs/VERSION.txt"
        const val VERSION_KEY = "LIBWCS.VERSION"

        @JvmStatic private val LOG = loggerFor<LibWCSDownloadTask>()

        @JvmStatic private val LIBRARY_URLS = mapOf(
            "linux-x86-64" to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/wcs/linux-x86-64/libwcs.so",
            "win32-x86-64" to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/wcs/win32-x86-64/libwcs.dll",
        )
    }
}
