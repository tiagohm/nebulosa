package nebulosa.desktop.logic.loader

import nebulosa.time.IERS
import nebulosa.time.IERSA
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class IERSLoader : Thread(), KoinComponent {

    private val appDirectory by inject<Path>(named("app"))
    private val path by lazy { Paths.get("$appDirectory", "finals2000A.all") }

    override fun run() {
        LOG.info("checking finals2000A.all")

        if (shouldBeDownloaded()) {
            LOG.info("downloading finals2000A.all")

            try {
                download()
            } catch (e: Throwable) {
                LOG.error("finals2000A.all download failed", e)
            }
        }
    }

    private fun shouldBeDownloaded(): Boolean {
        if (!path.exists()) {
            LOG.warn("finals2000A.all don't exists")
            return true
        }

        if (lastModifiedDateOfIERS() > path.toFile().lastModified()) {
            LOG.warn("finals2000A.all is out of date")
            return true
        }

        if (path.toFile().length() <= 0) {
            LOG.warn("finals2000A.all is empty")
            return true
        }

        return try {
            path.inputStream().use { IERSA.load(it) }
            IERS.current = IERSA
            LOG.info("finals2000A.all is loaded")
            false
        } catch (e: Throwable) {
            LOG.warn("finals2000A.all is corrupted")
            true
        }
    }

    private fun lastModifiedDateOfIERS(): Long {
        val request = Request.Builder()
            .head()
            .url(IERSA.URL)
            .build()

        return OK_HTTP_CLIENT.newCall(request)
            .execute()
            .use { it.headers.getDate("Last-Modified")?.time ?: 0L }
    }

    private fun download() {
        val request = Request.Builder()
            .get()
            .url(IERSA.URL)
            .build()

        OK_HTTP_CLIENT.newCall(request)
            .execute()
            .use { response ->
                path.outputStream().use {
                    val bytes = response.body.bytes()
                    bytes.inputStream().copyTo(it)
                    IERSA.load(bytes.inputStream())
                    IERS.current = IERSA
                    LOG.info("finals2000A.all is loaded")
                }
            }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(IERSLoader::class.java)
        @JvmStatic private val OK_HTTP_CLIENT = OkHttpClient.Builder().build()
    }
}
