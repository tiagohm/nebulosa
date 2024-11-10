import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteGroupType
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.database.migration.SkyDatabaseMigrator
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong

class SatelliteEntityRepositoryTest {

    @Test
    fun findAll() {
        REPOSITORY.search().shouldHaveSize(2)
    }

    @Test
    fun findByName() {
        REPOSITORY.search("iss").shouldHaveSize(1)
    }

    @Test
    fun findByGroups() {
        REPOSITORY.search(groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(2)
        REPOSITORY.search(groups = listOf(SatelliteGroupType.STARLINK)).shouldHaveSize(1)
        REPOSITORY.search(groups = listOf(SatelliteGroupType.AMATEUR)).shouldBeEmpty()
        REPOSITORY.search(groups = listOf(SatelliteGroupType.AMATEUR, SatelliteGroupType.STARLINK)).shouldHaveSize(1)
        REPOSITORY.search(groups = listOf(SatelliteGroupType.EDUCATION, SatelliteGroupType.STARLINK)).shouldHaveSize(2)
    }

    @Test
    fun findByNameAndGroups() {
        REPOSITORY.search(text = "iss", groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(1)
        REPOSITORY.search(text = "iss", groups = listOf(SatelliteGroupType.STARLINK)).shouldBeEmpty()
        REPOSITORY.search(text = "starlink", groups = listOf(SatelliteGroupType.EDUCATION)).shouldBeEmpty()
        REPOSITORY.search(text = "starlink", groups = listOf(SatelliteGroupType.ACTIVE, SatelliteGroupType.STARLINK)).shouldHaveSize(1)
    }

    companion object {

        internal val ISS_TLE = """
            ISS (ZARYA)
            1 25544U 98067A   24182.23525622  .00026310  00000+0  46754-3 0  9994
            2 25544  51.6392 250.6622 0011086  22.0936  34.8107 15.49934787460571
        """.trimIndent()

        private const val DATASOURCE = "jdbc:h2:mem:sat;DB_CLOSE_DELAY=-1"

        private val CONNECTION = Database.connect(DATASOURCE, user = "root", password = "")
        private val ID = AtomicLong(1)

        @AfterAll
        @JvmStatic
        fun closeConnection() {
            TransactionManager.closeAndUnregister(CONNECTION)
        }

        init {
            SkyDatabaseMigrator(DATASOURCE).run()
        }

        private val REPOSITORY = SatelliteRepository(CONNECTION).apply {
            save("ISS (ZARYA)", ISS_TLE, SatelliteGroupType.ACTIVE, SatelliteGroupType.EDUCATION)
            save("StarLink", "", SatelliteGroupType.ACTIVE, SatelliteGroupType.STARLINK)
        }

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun SatelliteRepository.save(name: String, tle: String = "", vararg groups: SatelliteGroupType) {
            add(SatelliteEntity(ID.getAndIncrement(), name, tle, groups.toList()))
        }
    }
}
