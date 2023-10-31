package nebulosa.api.atlas

import nebulosa.api.beans.annotations.ThreadedTask
import nebulosa.io.transferAndClose
import nebulosa.log.loggerFor
import nebulosa.time.IERS
import nebulosa.time.IERSA
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Component
@ThreadedTask
class IERSUpdater(
    private val dataPath: Path,
    private val httpClient: OkHttpClient,
) : Runnable {

    override fun run() {
        val finals2000A = Path.of("$dataPath", "finals2000A.all")

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

            httpClient.newCall(request).execute().use {
                it.body.byteStream().transferAndClose(outputStream())
                LOG.info("finals2000A.all loaded")
            }
        } catch (e: Throwable) {
            LOG.error("failed to download finals2000A.all", e)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<IERSUpdater>()
    }
}
