package nebulosa.desktop.logic.loader

import jakarta.annotation.PostConstruct
import nebulosa.common.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.Preferences
import nebulosa.skycatalog.stellarium.Nebula
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.gzip
import okio.source
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
class StellariumCatalogLoader : Runnable {

    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var okHttpClient: OkHttpClient
    @Autowired private lateinit var preferences: Preferences
    @Autowired private lateinit var systemExecutorService: ExecutorService
    @Autowired private lateinit var nebula: Nebula

    @PostConstruct
    override fun run() {
        val downloadLatch = CountUpDownLatch()

        val catalogPath = Paths.get("$appDirectory", "data", "stellarium", "catalog.dat")
        val namesPath = Paths.get("$appDirectory", "data", "stellarium", "names.dat")

        catalogPath.parent.createDirectories()

        val updatedAt = preferences.long("loader.stellarium.updatedAt") ?: 0L
        val currentTime = System.currentTimeMillis()
        val past30Days = currentTime - updatedAt >= 2592000000L

        if (past30Days || !catalogPath.exists() || !namesPath.exists()) {
            downloadLatch.countUp(2)

            systemExecutorService.submit {
                with(Request.Builder().url(CATALOG_URL).build()) {
                    try {
                        val response = okHttpClient.newCall(this).execute()

                        response.use {
                            LOG.info("catalog.dat downloaded successfully")
                            catalogPath.outputStream().use(it.body.byteStream()::transferTo)
                            preferences.long("loader.stellarium.updatedAt", currentTime)
                        }
                    } finally {
                        downloadLatch.countDown()
                    }
                }
            }

            systemExecutorService.submit {
                with(Request.Builder().url(NAMES_URL).build()) {
                    val response = okHttpClient.newCall(this).execute()

                    try {
                        response.use {
                            LOG.info("names.dat downloaded successfully")
                            namesPath.outputStream().use(response.body.byteStream()::transferTo)
                        }
                    } finally {
                        downloadLatch.countDown()
                    }
                }
            }
        } else {
            LOG.info("Stellarium DSO Catalog and Names are up-to-date")
        }

        systemExecutorService.submit {
            downloadLatch.await()

            if (catalogPath.exists()) {
                catalogPath.inputStream().use { catalog ->
                    if (namesPath.exists()) {
                        namesPath.inputStream().use { names ->
                            nebula.load(catalog.source().gzip(), names.source())
                            LOG.info("Stellarium DSO Catalog and Names loaded. size={} entries", nebula.size)
                        }
                    } else {
                        nebula.load(catalog.source().gzip())
                        LOG.info("Stellarium DSO Catalog loaded. size={} entries", nebula.size)
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
