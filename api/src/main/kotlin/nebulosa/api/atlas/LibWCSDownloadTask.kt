package nebulosa.api.atlas

import com.sun.jna.Platform
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.preference.PreferenceService
import nebulosa.io.transferAndCloseOutput
import nebulosa.log.e
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.wcs.LibWCS
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.codec.digest.DigestUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.nio.file.Path
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists
import kotlin.io.path.inputStream
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
            LOG.e("unable to download for arch {}", Platform.RESOURCE_PREFIX)
            return
        }

        val libraryPath = Path.of("$libsPath", libraryUrl.substring(libraryUrl.lastIndexOf('/') + 1))

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val newestVersion = response.body!!.string()

                if (newestVersion != preferenceService.getText(VERSION_KEY) || !libraryPath.exists()) {
                    LOG.i("libwcs is out of date. Downloading...")

                    request = Request.Builder().get().url(libraryUrl).build()

                    httpClient.newCall(request).execute().use {
                        if (it.isSuccessful) {
                            it.body!!.byteStream().transferAndCloseOutput(libraryPath.outputStream())
                            preferenceService.putText(VERSION_KEY, newestVersion)
                        }
                    }
                } else {
                    LOG.i("libwcs is up to date. version={}", newestVersion)
                }
            }
        }

        val checksum = libraryPath.inputStream().use { DigestUtils.sha256Hex(it) }

        if (checksum == LIBRARY_CHECKSUM[Platform.RESOURCE_PREFIX]) {
            LOG.i("libwcs checksum is valid!")
            System.setProperty(LibWCS.PATH, "$libraryPath")
        } else {
            LOG.e("failed to validate checksum. expected {}, got {}", LIBRARY_CHECKSUM[Platform.RESOURCE_PREFIX], checksum)
        }
    }

    companion object {

        const val VERSION_URL = "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/VERSION.txt"
        const val VERSION_KEY = "LIBWCS.VERSION"

        const val LINUX_X86_64 = "linux-x86-64"
        const val LINUX_AARCH_64 = "linux-aarch64"
        const val WIN_32_X86_64 = "win32-x86-64"

        private val LOG = loggerFor<LibWCSDownloadTask>()

        private val LIBRARY_URLS = mapOf(
            LINUX_X86_64 to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/$LINUX_X86_64/libwcs.so",
            LINUX_AARCH_64 to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/$LINUX_AARCH_64/libwcs.so",
            WIN_32_X86_64 to "https://raw.githubusercontent.com/tiagohm/nebulosa.data/main/libs/wcs/$WIN_32_X86_64/libwcs.dll",
        )

        private val LIBRARY_CHECKSUM = mapOf(
            LINUX_X86_64 to "ca74289426e9536eb8a38b6fe866d3bb8478400424f6652f7d9db007fee342f4",
            LINUX_AARCH_64 to "8a5d14a22dcb9656b32519167a98ad2489cfd9262a4336ac3717a2eb3bf7354e",
            WIN_32_X86_64 to "65ee5696485a1b2bdc5248a581bb43c947615f95051dd7efca669da475b775ab",
        )
    }
}
