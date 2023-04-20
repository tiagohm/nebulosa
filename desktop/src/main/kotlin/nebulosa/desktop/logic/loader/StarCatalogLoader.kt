package nebulosa.desktop.logic.loader

import jakarta.annotation.PostConstruct
import nebulosa.desktop.model.DsoEntity
import nebulosa.desktop.model.NameEntity
import nebulosa.desktop.model.StarEntity
import nebulosa.io.resource
import nebulosa.io.transferAndClose
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.sqlite.JDBC
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

@Service
class StarCatalogLoader : Runnable {

    @Autowired private lateinit var appDirectory: Path
    @Autowired private lateinit var systemExecutorService: ExecutorService

    @PostConstruct
    override fun run() {
        systemExecutorService.execute {
            val catalog = Paths.get("$appDirectory", "data", "catalog", "StarCatalog.db")

            catalog.parent.createDirectories()

            resource("data/StarCatalog.db")!!.transferAndClose(catalog.outputStream())

            Database.connect("jdbc:sqlite:$catalog", driver = JDBC::class.java.name)

            transaction {
                LOG.info(
                    "star catalog loaded. dsos={}, stars={}, names={}",
                    DsoEntity.selectAll().count(), StarEntity.selectAll().count(), NameEntity.selectAll().count(),
                )
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(StarCatalogLoader::class.java)
    }
}
