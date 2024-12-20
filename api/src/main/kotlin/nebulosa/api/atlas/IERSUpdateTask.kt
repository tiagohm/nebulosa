package nebulosa.api.atlas

import io.ktor.http.HttpHeaders
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.preference.PreferenceService
import nebulosa.io.transferAndClose
import nebulosa.log.d
import nebulosa.log.e
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.time.IERS
import nebulosa.time.IERSA
import nebulosa.time.IERSAB
import nebulosa.time.IERSB
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.nio.file.Path
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class IERSUpdateTask(
    private val dataPath: Path,
    private val httpClient: OkHttpClient,
    private val preferenceService: PreferenceService,
    scheduledExecutorService: ScheduledExecutorService,
) : Runnable, KoinComponent {

    init {
        scheduledExecutorService.schedule(this, 5L, TimeUnit.SECONDS)
    }

    override fun run() {
        get<MainDatabaseMigrator>().await()

        val iersa = IERSA()
        val iersb = IERSB()

        with(Path.of("$dataPath", "finals2000A.all")) {
            download(IERSA.URL, IERSA_UPDATED_AT_KEY)
            inputStream().use(iersa::load)
        }

        with(Path.of("$dataPath", "eopc04.1962-now.txt")) {
            download(IERSB.URL, IERSB_UPDATED_AT_KEY)
            inputStream().use(iersb::load)
        }

        IERS.attach(IERSAB(iersa, iersb))
    }

    private fun Path.download(url: String, key: String) {
        try {
            var request = Request.Builder().head().url(url).build()

            var modifiedAt = httpClient.newCall(request).execute()
                .use { it.headers.getDate(HttpHeaders.LastModified) }

            if (modifiedAt != null && "$modifiedAt" == preferenceService.getText(key)) {
                LOG.i("{} is up to date. modifiedAt={}", url, modifiedAt)
                return
            }

            request = request.newBuilder().get().build()

            LOG.d("downloading {}", url)

            httpClient.newCall(request).execute().use {
                it.body!!.byteStream().transferAndClose(outputStream())
                modifiedAt = it.headers.getDate(HttpHeaders.LastModified)
                preferenceService.putText(key, "$modifiedAt")
                LOG.d("{} downloaded. modifiedAt={}", url, modifiedAt)
            }
        } catch (e: Throwable) {
            LOG.e("failed to download finals2000A.all", e)
        }
    }

    companion object {

        const val IERSA_UPDATED_AT_KEY = "IERSA.UPDATED_AT"
        const val IERSB_UPDATED_AT_KEY = "IERSB.UPDATED_AT"

        private val LOG = loggerFor<IERSUpdateTask>()
    }
}
