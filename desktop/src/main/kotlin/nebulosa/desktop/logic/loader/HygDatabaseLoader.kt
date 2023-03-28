package nebulosa.desktop.logic.loader

import jakarta.annotation.PostConstruct
import nebulosa.skycatalog.hyg.HygDatabase
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream

@Service
class HygDatabaseLoader : Runnable {

    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var okHttpClient: OkHttpClient
    @Autowired private lateinit var hygDatabase: HygDatabase
    @Autowired private lateinit var systemExecutorService: ExecutorService

    @PostConstruct
    override fun run() {
        val catalogPath = Paths.get("$appDirectory", "data", "hyg", "hygdata_v3.csv")

        catalogPath.parent.createDirectories()

        if (catalogPath.exists()) {
            systemExecutorService.submit {
                hygDatabase.load(catalogPath)
                LOG.info("hyg database loaded. size={} stars", hygDatabase.size)
            }
        } else {
            LOG.info("downloading hyg database")

            systemExecutorService.submit {
                with(Request.Builder().url(DATABASE_URL).build()) {
                    val response = okHttpClient.newCall(this).execute()

                    response.use {
                        LOG.info("hyg database downloaded successfully")
                        catalogPath.outputStream().use(it.body.byteStream()::transferTo)
                        hygDatabase.load(catalogPath)
                        LOG.info("hyg database loaded. size={} stars", hygDatabase.size)
                    }
                }
            }
        }
    }

    companion object {

        private const val DATABASE_URL = "https://github.com/astronexus/HYG-Database/raw/master/hygdata_v3.csv"

        @JvmStatic private val LOG = LoggerFactory.getLogger(HygDatabaseLoader::class.java)
    }
}
