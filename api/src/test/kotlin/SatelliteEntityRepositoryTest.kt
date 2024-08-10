import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.objectbox.kotlin.boxFor
import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteGroupType
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.database.MyObjectBox
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.util.*

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
    }

    @Test
    fun findByNameAndGroups() {
        REPOSITORY.search(text = "iss", groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(1)
        REPOSITORY.search(text = "iss", groups = listOf(SatelliteGroupType.STARLINK)).shouldBeEmpty()
        REPOSITORY.search(text = "starlink", groups = listOf(SatelliteGroupType.EDUCATION)).shouldBeEmpty()
    }

    companion object {

        @JvmStatic internal val ISS_TLE = """
            ISS (ZARYA)
            1 25544U 98067A   24182.23525622  .00026310  00000+0  46754-3 0  9994
            2 25544  51.6392 250.6622 0011086  22.0936  34.8107 15.49934787460571
        """.trimIndent()

        @JvmStatic private val BOX_STORE = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        @AfterAll
        @JvmStatic
        fun closeBoxStore() {
            BOX_STORE.close()
        }

        @JvmStatic private val BOX = BOX_STORE.boxFor<SatelliteEntity>()
        @JvmStatic private val REPOSITORY = SatelliteRepository(BOX).apply {
            save("ISS (ZARYA)", ISS_TLE, SatelliteGroupType.ACTIVE, SatelliteGroupType.EDUCATION)
            save("StarLink", "", SatelliteGroupType.ACTIVE, SatelliteGroupType.STARLINK)
        }

        @JvmStatic
        internal fun SatelliteRepository.save(name: String, tle: String = "", vararg groups: SatelliteGroupType) {
            save(SatelliteEntity(0L, name, tle, groups.map { it.name }.toMutableList()))
        }
    }
}
