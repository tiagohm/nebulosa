package nebulosa.api.components.loaders

import jakarta.annotation.PostConstruct
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.time.IERS
import nebulosa.time.IERSA
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Component
class IERSLoader(
    private val dataDirectory: Path,
    private val okHttpClient: OkHttpClient,
    private val systemExecutorService: ExecutorService,
) : Runnable {

    @PostConstruct
    private fun initialize() {
        systemExecutorService.submit(this)
    }

    override fun run() {
        val finals2000A = Path.of("$dataDirectory", "finals2000A.all")

        finals2000A.download()

        val iersa = IERSA()
        finals2000A.inputStream().use(iersa::load)
        IERS.attach(iersa)
    }

    private fun Path.download() {
        val request = Request.Builder()
            .get()
            .url(IERSA.URL)
            .build()

        try {
            LOG.info("downloading finals2000A.all")

            okHttpClient.newCall(request).execute().use {
                it.body.byteStream().transferAndClose(outputStream())
                LOG.info("finals2000A.all loaded")
            }
        } catch (e: Throwable) {
            LOG.error("failed to download finals2000A.all", e)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<IERSLoader>()
    }
}
