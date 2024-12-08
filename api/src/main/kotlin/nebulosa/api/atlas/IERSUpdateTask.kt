package nebulosa.api.atlas

import io.ktor.http.*
import nebulosa.api.database.migration.MainDatabaseMigrator
import nebulosa.api.preference.PreferenceService
import nebulosa.io.transferAndClose
import nebulosa.log.d
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
import kotlin.io.path.exists
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

        var iersa: IERSA? = null
        var iersb: IERSB? = null

        with(Path.of("$dataPath", "finals2000A.all")) {
            if (download(IERSA.URL, IERSA_UPDATED_AT_KEY)) {
                iersa = IERSA()
                inputStream().use(iersa::load)
            }
        }

        with(Path.of("$dataPath", "eopc04.1962-now.txt")) {
            if (download(IERSB.URL, IERSB_UPDATED_AT_KEY)) {
                iersb = IERSB()
                inputStream().use(iersb::load)
            }
        }

        if (iersa != null && iersb != null) IERS.attach(IERSAB(iersa, iersb))
        else if (iersa != null) IERS.attach(iersa)
        else if (iersb != null) IERS.attach(iersb)
    }

    private fun Path.download(url: String, key: String): Boolean {
        try {
            var request = Request.Builder().head().url(url).build()

            var modifiedAt = httpClient.newCall(request).execute()
                .use { it.headers.getDate(HttpHeaders.LastModified) }
                ?.toInstant()?.toEpochMilli()

            if (exists() && modifiedAt != null && modifiedAt == preferenceService[key]?.toLongOrNull()) {
                LOG.info("{} is up to date. modifiedAt={}", url, modifiedAt)
                return true
            }

            request = request.newBuilder().get().build()

            LOG.d { debug("{} is out of date. modifiedAt={}", url, modifiedAt) }

            httpClient.newCall(request).execute().use {
                if (it.isSuccessful) {
                    it.body!!.byteStream().transferAndClose(outputStream())
                    modifiedAt = it.headers.getDate(HttpHeaders.LastModified)?.toInstant()?.toEpochMilli()

                    if (modifiedAt != null) {
                        preferenceService[key] = modifiedAt
                        preferenceService.save()
                    }

                    LOG.d { debug("{} downloaded. modifiedAt={}", url, modifiedAt) }

                    return true
                }
            }
        } catch (e: Throwable) {
            LOG.error("failed to download finals2000A.all", e)
        }

        return exists()
    }

    companion object {

        const val IERSA_UPDATED_AT_KEY = "iersa.updatedAt"
        const val IERSB_UPDATED_AT_KEY = "iersb.updatedAt"

        private val LOG = loggerFor<IERSUpdateTask>()
    }
}
