package nebulosa.api.atlas

import nebulosa.api.preferences.PreferenceService
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.time.IERS
import nebulosa.time.IERSA
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Component
class IERSUpdateTask(
    private val dataPath: Path,
    private val preferenceService: PreferenceService,
    private val httpClient: OkHttpClient,
) : Runnable {

    @Scheduled(initialDelay = 5L, fixedDelay = Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    override fun run() {
        val finals2000A = Path.of("$dataPath", "finals2000A.all")

        finals2000A.download()

        val iersa = IERSA()
        finals2000A.inputStream().use(iersa::load)
        IERS.attach(iersa)
    }

    private fun Path.download() {
        try {
            var request = Request.Builder()
                .head()
                .url(IERSA.URL)
                .build()

            var modifiedAt = httpClient.newCall(request).execute()
                .use { it.headers.getDate(HttpHeaders.LAST_MODIFIED) }

            if (modifiedAt != null && "$modifiedAt" == preferenceService.getText(IERS_UPDATED_AT)) {
                LOG.info("finals2000A.all is up to date. modifiedAt={}", modifiedAt)
                return
            }

            request = request.newBuilder().get().build()

            LOG.info("downloading finals2000A.all")

            httpClient.newCall(request).execute().use {
                it.body!!.byteStream().transferAndClose(outputStream())
                modifiedAt = it.headers.getDate(HttpHeaders.LAST_MODIFIED)
                preferenceService.putText(IERS_UPDATED_AT, "$modifiedAt")
                LOG.info("finals2000A.all downloaded. modifiedAt={}", modifiedAt)
            }
        } catch (e: Throwable) {
            LOG.error("failed to download finals2000A.all", e)
        }
    }

    companion object {

        const val IERS_UPDATED_AT = "IERS_UPDATED_AT"

        @JvmStatic private val LOG = loggerFor<IERSUpdateTask>()
    }
}
