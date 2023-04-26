package nebulosa.desktop.logic.loader

import kotlinx.coroutines.runBlocking
import nebulosa.desktop.helper.await
import nebulosa.desktop.service.PreferenceService
import nebulosa.io.transferAndClose
import nebulosa.time.IERS
import nebulosa.time.IERSA
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
@EnableScheduling
class IERSLoader : Runnable {

    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var preferenceService: PreferenceService
    @Autowired private lateinit var okHttpClient: OkHttpClient

    @Scheduled(fixedRate = 1L, initialDelay = 0L, timeUnit = TimeUnit.HOURS)
    override fun run() {
        val finals2000A = Paths.get("$appDirectory", "data", "iers", "finals2000A.all")

        finals2000A.parent.createDirectories()

        LOG.info("checking finals2000A.all")

        runBlocking {
            if (finals2000A.shouldBeDownloaded()) {
                LOG.info("downloading finals2000A.all")
                finals2000A.download()
            }

            try {
                val iersa = IERSA()
                finals2000A.inputStream().use(iersa::load)
                IERS.attach(iersa)

                LOG.info("finals2000A.all loaded")
            } catch (e: Throwable) {
                LOG.error("failed to load finals2000A.all", e)
            }
        }
    }

    private suspend fun Path.shouldBeDownloaded(): Boolean {
        return LAST_MODIFIED_KEY !in preferenceService
                || !exists()
                || preferenceService.long(LAST_MODIFIED_KEY) != lastModifiedDate()
    }

    private suspend fun lastModifiedDate(): Long {
        val request = Request.Builder()
            .head()
            .url(IERSA.URL)
            .build()

        return try {
            okHttpClient.newCall(request).await().use {
                it.headers.getDate("Last-Modified")?.time ?: System.currentTimeMillis()
            }
        } catch (e: Throwable) {
            LOG.error("failed to head finals2000A.all", e)
            System.currentTimeMillis()
        }
    }

    private suspend fun Path.download() {
        val request = Request.Builder()
            .get()
            .url(IERSA.URL)
            .build()

        try {
            okHttpClient.newCall(request).await().use {
                it.body.byteStream().transferAndClose(outputStream())
                val lastModified = it.headers.getDate("Last-Modified")?.time
                if (lastModified != null) preferenceService.long(LAST_MODIFIED_KEY, lastModified)
            }
        } catch (e: Throwable) {
            LOG.error("failed to download finals2000A.all", e)
        }
    }

    companion object {

        private const val LAST_MODIFIED_KEY = "iersLoader.lastModified"

        @JvmStatic private val LOG = LoggerFactory.getLogger(IERSLoader::class.java)
    }
}
