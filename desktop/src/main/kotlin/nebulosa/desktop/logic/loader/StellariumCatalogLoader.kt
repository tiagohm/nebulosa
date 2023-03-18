package nebulosa.desktop.logic.loader

import jakarta.annotation.PostConstruct
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.stellarium.skycatalog.Nebula
import okhttp3.*
import okio.IOException
import okio.gzip
import okio.source
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
class StellariumCatalogLoader : Runnable {

    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var okHttpClient: OkHttpClient
    @Autowired private lateinit var nebula: Nebula

    @PostConstruct
    override fun run() {
        val downloadLatch = CountUpDownLatch()

        downloadLatch.countUp()
        downloadLatch.countUp()

        val catalogPath = Paths.get("$appDirectory", "data", "stellarium", "catalog.dat")
        val namesPath = Paths.get("$appDirectory", "data", "stellarium", "names.dat")

        catalogPath.parent.createDirectories()

        with(Request.Builder().url(CATALOG_URL).build()) {
            okHttpClient.newCall(this).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LOG.error("failed to download catalog.dat", e)
                    downloadLatch.countDown()
                }

                override fun onResponse(call: Call, response: Response) {
                    LOG.info("catalog.dat downloaded successfully")
                    catalogPath.outputStream().use { response.body.byteStream().transferTo(it) }
                    downloadLatch.countDown()
                }
            })
        }

        with(Request.Builder().url(NAMES_URL).build()) {
            okHttpClient.newCall(this).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LOG.error("failed to download names.dat", e)
                    downloadLatch.countDown()
                }

                override fun onResponse(call: Call, response: Response) {
                    LOG.info("names.dat downloaded successfully")
                    namesPath.outputStream().use { response.body.byteStream().transferTo(it) }
                    downloadLatch.countDown()
                }
            })
        }

        CompletableFuture.supplyAsync {
            downloadLatch.await()

            if (catalogPath.exists()) {
                catalogPath.inputStream().use { catalog ->
                    if (namesPath.exists()) {
                        namesPath.inputStream().use { names ->
                            LOG.info("loading Stellarium DSO Catalog and Names")
                            nebula.load(catalog.source().gzip(), names.source())
                        }
                    } else {
                        LOG.info("loading Stellarium DSO Catalog only")
                        nebula.load(catalog.source().gzip())
                    }
                }
            } else {
                LOG.warn("unable to load Stellarium DSO Catalog")
            }
        }
    }

    companion object {

        private const val CATALOG_URL = "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/catalog.dat"
        private const val NAMES_URL = "https://github.com/Stellarium/stellarium/raw/master/nebulae/default/names.dat"

        @JvmStatic private val LOG = LoggerFactory.getLogger(StellariumCatalogLoader::class.java)
    }
}
