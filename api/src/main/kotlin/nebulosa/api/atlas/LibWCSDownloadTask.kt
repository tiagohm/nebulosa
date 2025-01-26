package nebulosa.api.atlas

import com.sun.jna.Platform
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.preference.PreferenceService
import nebulosa.io.transferAndCloseOutput
import nebulosa.log.loggerFor
import nebulosa.wcs.LibWCS
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
) : Runnable, KoinComponent {

    init {
        scheduledExecutorService.schedule(this, 5L, TimeUnit.SECONDS)
    }

    override fun run() {
        get<MainDatabaseMigrator>().await()

        var request = Request.Builder().get().url(VERSION_URL).build()

        val libraryUrl = LIBRARY_URLS[Platform.RESOURCE_PREFIX]

        if (libraryUrl.isNullOrBlank()) {
            LOG.error("unable to download for arch {}", Platform.RESOURCE_PREFIX)
            return
        }

        val libraryPath = Path.of("$libsPath", libraryUrl.substring(libraryUrl.lastIndexOf('/') + 1))

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val newestVersion = response.body!!.string()

                if (newestVersion != preferenceService[VERSION_KEY] || !libraryPath.exists()) {
                    LOG.info("libwcs is out of date. Downloading...")

                    request = Request.Builder().get().url(libraryUrl).build()

                    httpClient.newCall(request).execute().use {
                        if (it.isSuccessful) {
                            it.body!!.byteStream().transferAndCloseOutput(libraryPath.outputStream())
                            preferenceService[VERSION_KEY] = newestVersion
                            preferenceService.save()
                        }
                    }
                } else {
                    LOG.info("libwcs is up to date. version={}", newestVersion)
                }
            }
        }

        System.setProperty(LibWCS.PATH, "$libraryPath")
    }

    companion object {

        const val VERSION_URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/VERSION.txt"
        const val VERSION_KEY = "libwcs.version"

        const val LINUX_X86_64 = "linux-x86-64"
        const val LINUX_AARCH_64 = "linux-aarch64"
        const val WIN_32_X86_64 = "win32-x86-64"

        private val LOG = loggerFor<LibWCSDownloadTask>()

        private val LIBRARY_URLS = mapOf(
            LINUX_X86_64 to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/$LINUX_X86_64/libwcs.so",
            LINUX_AARCH_64 to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/$LINUX_AARCH_64/libwcs.so",
            WIN_32_X86_64 to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/$WIN_32_X86_64/libwcs.dll",
        )
    }
}
