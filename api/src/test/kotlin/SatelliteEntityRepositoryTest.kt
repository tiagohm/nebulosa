import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.objectbox.kotlin.boxFor
import nebulosa.api.atlas.SatelliteEntity
import nebulosa.api.atlas.SatelliteGroupType
import nebulosa.api.atlas.SatelliteRepository
import nebulosa.api.database.MyObjectBox
import java.util.*

class SatelliteEntityRepositoryTest : StringSpec() {

    init {
        val boxStore = MyObjectBox.builder()
            .inMemory(UUID.randomUUID().toString())
            .build()

        afterSpec {
            boxStore.close()
        }

        val box = boxStore.boxFor<SatelliteEntity>()
        val repository = SatelliteRepository(box)

        repository.save("ISS (ZARYA)", ISS_TLE, SatelliteGroupType.ACTIVE, SatelliteGroupType.EDUCATION)
        repository.save("StarLink", "", SatelliteGroupType.ACTIVE, SatelliteGroupType.STARLINK)

        "find all" {
            repository.search().shouldHaveSize(2)
        }
        "find by name" {
            repository.search("iss").shouldHaveSize(1)
        }
        "find by groups" {
            repository.search(groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(2)
            repository.search(groups = listOf(SatelliteGroupType.STARLINK)).shouldHaveSize(1)
            repository.search(groups = listOf(SatelliteGroupType.AMATEUR)).shouldBeEmpty()
        }
        "find by name and groups" {
            repository.search(text = "iss", groups = listOf(SatelliteGroupType.ACTIVE)).shouldHaveSize(1)
            repository.search(text = "iss", groups = listOf(SatelliteGroupType.STARLINK)).shouldBeEmpty()
            repository.search(text = "starlink", groups = listOf(SatelliteGroupType.EDUCATION)).shouldBeEmpty()
        }
    }

    companion object {

        @JvmStatic internal val ISS_TLE = """
            ISS (ZARYA)
            1 25544U 98067A   24182.23525622  .00026310  00000+0  46754-3 0  9994
            2 25544  51.6392 250.6622 0011086  22.0936  34.8107 15.49934787460571
        """.trimIndent()

        @JvmStatic
        internal fun SatelliteRepository.save(name: String, tle: String = "", vararg groups: SatelliteGroupType) {
            save(SatelliteEntity(0L, name, tle, groups.map { it.name }.toMutableList()))
        }
    }
}
