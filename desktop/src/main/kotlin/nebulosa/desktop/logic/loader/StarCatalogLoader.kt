package nebulosa.desktop.logic.loader

import jakarta.annotation.PostConstruct
import nebulosa.desktop.model.DeepSkyObjects
import nebulosa.desktop.model.Names
import nebulosa.desktop.model.Stars
import nebulosa.io.resourceUrl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.sqlite.JDBC

@Service
class StarCatalogLoader : Runnable {

    @PostConstruct
    override fun run() {
        val catalog = resourceUrl("data/star.catalog.db")!!.toExternalForm()

        Database
            .connect(
                "jdbc:sqlite:$catalog",
                driver = JDBC::class.java.name
            )

        transaction {
            LOG.info(
                "star catalog loaded. dsos={}, stars={}, names={}",
                DeepSkyObjects.selectAll().count(),
                Stars.selectAll().count(),
                Names.selectAll().count(),
            )
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(StarCatalogLoader::class.java)
    }
}
