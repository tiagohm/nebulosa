import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.api.atlas.SkyObjectEntity
import nebulosa.api.atlas.SkyObjectEntityRepository
import nebulosa.api.database.migration.SkyDatabaseMigrator
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.hours
import nebulosa.nova.astrometry.Constellation
import nebulosa.skycatalog.SkyObjectType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class SkyObjectEntityRepositoryTest {

    @Test
    fun findAll() {
        REPOSITORY.search().shouldHaveSize(4).first().magnitude shouldBeExactly -1.45
    }

    @Test
    fun findByName() {
        REPOSITORY.search(name = "dolphin").shouldHaveSize(1).first().name shouldBe listOf("Dolphin Nebula")
        REPOSITORY.search(name = "In nE").shouldHaveSize(1).first().name shouldBe listOf("Dolphin Nebula")
        REPOSITORY.search(name = "andromeda").shouldBeEmpty()
        REPOSITORY.search(name = "nebula").shouldHaveSize(2).first().magnitude shouldBeExactly 5.0
    }

    @Test
    fun findByConstellation() {
        REPOSITORY.search(constellation = Constellation.CMA).shouldHaveSize(2).first().magnitude shouldBeExactly -1.45
        REPOSITORY.search(constellation = Constellation.AND).shouldBeEmpty()
    }

    @Test
    fun findByRegion() {
        REPOSITORY.search(rightAscension = "06 45 59".hours, declination = "-20 45 29".deg, radius = 4.5.deg).shouldHaveSize(2)
            .first().magnitude shouldBeExactly -1.45
        REPOSITORY.search(rightAscension = "06 45 59".hours, declination = "-20 45 29".deg, radius = 4.0.deg).shouldHaveSize(1)
            .first().name shouldBe listOf("Dolphin Nebula")
        REPOSITORY.search(rightAscension = "06 45 59".hours, declination = "-20 45 29".deg, radius = 1.0.deg).shouldBeEmpty()
        REPOSITORY.search(rightAscension = "00 42 43".hours, declination = "41 15 53".deg, radius = 10.deg).shouldBeEmpty()
    }

    @Test
    fun findByMagnitude() {
        REPOSITORY.search(magnitudeMin = 5.0).shouldHaveSize(3)
        REPOSITORY.search(magnitudeMax = 4.9).shouldHaveSize(1).first().name shouldBe listOf("Sirius")
        REPOSITORY.search(magnitudeMin = 6.6, magnitudeMax = 6.99).shouldHaveSize(1).first().name shouldBe listOf("Dolphin Nebula")
        REPOSITORY.search(magnitudeMax = -2.0).shouldBeEmpty()
        REPOSITORY.search(magnitudeMin = 7.0).shouldBeEmpty()
        REPOSITORY.search(magnitudeMin = 5.1, magnitudeMax = 6.0).shouldBeEmpty()
    }

    @Test
    fun findByType() {
        REPOSITORY.search(type = SkyObjectType.NEBULA).shouldHaveSize(2).first().magnitude shouldBeExactly 5.0
        REPOSITORY.search(type = SkyObjectType.GALAXY).shouldBeEmpty()
    }

    companion object {

        private const val DATASOURCE = "jdbc:h2:mem:sky;DB_CLOSE_DELAY=-1"

        private val CONNECTION = Database.connect(DATASOURCE, user = "root", password = "")

        @AfterAll
        @JvmStatic
        fun closeConnection() {
            TransactionManager.closeAndUnregister(CONNECTION)
        }

        init {
            SkyDatabaseMigrator(DATASOURCE).run()
        }

        private val REPOSITORY = SkyObjectEntityRepository(CONNECTION).apply {
            save("Sirius", SkyObjectType.STAR, Constellation.CMA, -1.45, "06 45 06".hours, "-16 43 33".deg)
            save("Dolphin Nebula", SkyObjectType.NEBULA, Constellation.CMA, 6.91, "06 54 11".hours, "-23 55 47".deg)
            save("75 Tucanae", SkyObjectType.GLOBULAR_CLUSTER, Constellation.TUC, 6.58, "01 03 12".hours, "-70 50 39".deg)
            save("Car Nebula", SkyObjectType.NEBULA, Constellation.CAR, 5.0, "10 45 15".hours, "-59 43 35".deg)
        }

        internal fun SkyObjectEntityRepository.save(
            name: String, type: SkyObjectType, constellation: Constellation,
            magnitude: Double, rightAscension: Angle, declination: Angle,
        ) = add(SkyObjectEntity(0L, listOf(name), type, rightAscension, declination, magnitude, constellation = constellation))
    }
}
